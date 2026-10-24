package com.flannaghan.cheetah.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.datasource.DataSourcesManager
import com.flannaghan.cheetah.common.datasource.DefinitionDataSource
import com.flannaghan.cheetah.common.datasource.dataSources
import com.flannaghan.cheetah.common.search.SearchContext
import com.flannaghan.cheetah.common.search.SearchResult
import com.flannaghan.cheetah.common.search.search
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(platform: Platform) : ViewModel() {
    val dataSourcesManager = DataSourcesManager(dataSources(platform))

    private val definitionDataSources = dataSourcesManager.dataSources.filterIsInstance<DefinitionDataSource>()
    private val fullTextSearchDataSource = definitionDataSources.firstOrNull { it.defaults.useDefinitions }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _wordListDataSources =
        MutableStateFlow(dataSourcesManager.dataSources.filter { it.defaults.useWordList }.toSet())
    val wordListDataSources: StateFlow<Set<DataSource>> = _wordListDataSources.asStateFlow()

    private val _result = MutableStateFlow(SearchResult(true, "", emptyList()))
    val result: StateFlow<SearchResult> = _result.asStateFlow()

    private val _selectedWordIndex = MutableStateFlow<Int?>(null)
    val selectedWordIndex: StateFlow<Int?> = _selectedWordIndex.asStateFlow()

    private val _definition = MutableStateFlow("")
    val definition: StateFlow<String> = _definition.asStateFlow()

    // Only the search collector below uses this, and collectLatest runs one search at a time.
    private var cachedSearchContext: Pair<Set<DataSource>, SearchContext>? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            // Loading the word lists takes a while, so start before the first search needs them.
            dataSourcesManager.getAllWords(_wordListDataSources.value.toList())
        }
        viewModelScope.launch {
            // A new query or data source selection cancels the search in progress.
            combine(_query, _wordListDataSources, ::Pair).collectLatest { (query, dataSources) ->
                _result.value = withContext(Dispatchers.Default) { runSearch(query, dataSources) }
            }
        }
        viewModelScope.launch {
            combine(_result, _selectedWordIndex) { result, index -> index?.let { result.words.getOrNull(it) } }
                .collectLatest { word ->
                    _definition.value = if (word == null) "" else withContext(Dispatchers.Default) {
                        lookupDefinition(word)
                    }
                }
        }
    }

    fun updateQuery(query: String) {
        _query.value = query
        _selectedWordIndex.value = 0
    }

    fun selectWord(index: Int) {
        _selectedWordIndex.value = index
    }

    fun toggleWordListDataSource(dataSource: DataSource) {
        _wordListDataSources.update { if (dataSource in it) it - dataSource else it + dataSource }
    }

    private suspend fun runSearch(query: String, dataSources: Set<DataSource>): SearchResult {
        if (query.isBlank()) return SearchResult(true, query, emptyList())
        val context = searchContext(dataSources)
        return context.withEvaluation { search(query, context) }
    }

    /**
     * Returns the SearchContext for the data sources. It is expensive to build, so the last one is reused until the
     * data sources change.
     */
    private suspend fun searchContext(dataSources: Set<DataSource>): SearchContext {
        cachedSearchContext?.let { (cachedDataSources, context) -> if (cachedDataSources == dataSources) return context }
        val context = SearchContext(
            dataSourcesManager.getAllWords(dataSources.toList()),
            fullTextSearch = fullTextSearchDataSource?.let { source ->
                { query, words -> source.fullTextSearch(words, query) }
            },
            relationshipSearch = { query, words ->
                definitionDataSources.flatMap { it.findLinkedWords(words, query) }.distinct()
            },
        )
        cachedSearchContext = dataSources to context
        return context
    }

    private suspend fun lookupDefinition(word: Word): String = coroutineScope {
        definitionDataSources
            .filter { it.defaults.useDefinitions }
            .map { async { it.lookupDefinition(word) } }
            .awaitAll()
            .joinToString("\n")
    }
}
