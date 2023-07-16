package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.datasource.DataSourcesManager
import com.flannaghan.cheetah.common.datasource.DefinitionDataSource
import com.flannaghan.cheetah.common.datasource.dataSources
import com.flannaghan.cheetah.common.search.SearchContext
import com.flannaghan.cheetah.common.search.SearchResult
import com.flannaghan.cheetah.common.search.search
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class SearchModel(private val context: ApplicationContext, scope: CoroutineScope) {
    @Composable
    abstract fun queryState(): State<String>

    @Composable
    abstract fun resultState(): State<SearchResult>

    @Composable
    abstract fun definitionState(): State<String>

    @Composable
    abstract fun wordListDataSources(): State<Set<DataSource>>


    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)
    abstract fun updateDefinition(definition: String)
    abstract fun updateWordListDataSources(dataSources: Set<DataSource>)

    val dataSourcesManager = DataSourcesManager(dataSources(context))
    private val ftsDataSource = dataSourcesManager.dataSources.filter {
        it.defaults.useDefinitions
    }.filterIsInstance<DefinitionDataSource>().firstOrNull()

    /**
     * Some initialisation that takes a while. Good to get on with this and not wait until the first search.
     */
    private val asyncInitialisation = scope.async {
        val dataSources = dataSourcesManager.dataSources.filter { it.defaults.useWordList }
        updateWordListDataSources(dataSources.toSet())
        withContext(backgroundContext()) {
            // Warm the cache up with the initially selected data sources.
            dataSourcesManager.getAllWords(dataSources, context)
        }
    }

    /**
     * Returns the SearchContext for the data sources. This caches the previous value returned, so unless the
     * data sources change, it'll return the same SearchContext (which is good because it is expensive to init).
     */
    private val searchContextLock = Mutex(false)
    private var searchContext: SearchContext? = null
    private var currentWordListDataSources: Set<DataSource> = emptySet()

    private suspend fun getSearchContext(wordListDataSources: Set<DataSource>): SearchContext {
        searchContextLock.withLock {
            val currentSearchContext = searchContext
            if (wordListDataSources != currentWordListDataSources || currentSearchContext == null) {
                val allWords = withContext(backgroundContext()) {
                    dataSourcesManager.getAllWords(wordListDataSources.toList(), context)
                }
                val thisSearchContext = SearchContext(
                    allWords,
                    fullTextSearch =  ftsDataSource?.let {
                        { query, words -> it.fullTextSearch(context, words, query) }
                    },
                    relationshipSearch = {
                        query, words ->
                        val result = mutableSetOf<Word>()
                        for (source in dataSourcesManager.dataSources.filterIsInstance<DefinitionDataSource>()) {
                            result.addAll(source.findLinkedWords(context, words, query))
                        }
                        result.toList()
                    }
                )
                currentWordListDataSources = wordListDataSources
                searchContext = thisSearchContext
                return thisSearchContext
            } else {
                return currentSearchContext
            }
        }
    }

    private var currentJobQuery: String? = null

    private val searchLauncher = SingleJobLauncher()
    private val definitionLookupLauncher = SingleJobLauncher()

    suspend fun doSearch(query: String, dataSources: Set<DataSource>) = coroutineScope {
        if (query == currentJobQuery) return@coroutineScope
        searchLauncher.launch(this) {
            val newResult = withContext(backgroundContext()) {
                asyncInitialisation.await()
                getSearchContext(dataSources).let {
                    it.withEvaluation { search(query, it) }
                }
            }
            updateResult(newResult)
        }
    }

    suspend fun lookupDefinition(word: Word) = coroutineScope {
        definitionLookupLauncher.launch(this) {
            val definitions = withContext(backgroundContext()) {
                dataSourcesManager.dataSources
                    .filter { it.defaults.useDefinitions }
                    .filterIsInstance<DefinitionDataSource>()
                    .map { async { it.lookupDefinition(context, word) } }
                    .awaitAll()
            }
            updateDefinition(definitions.joinToString("\n"))
        }
    }
}


/**
 * A wrapper that ensures only a single job is running at once.
 */
class SingleJobLauncher {
    var currentJob: Job? = null
    fun launch(scope: CoroutineScope, block: suspend CoroutineScope.() -> Unit): Job {
        currentJob?.cancel()
        val job = scope.launch(block = block)
        currentJob = job
        return job
    }
}