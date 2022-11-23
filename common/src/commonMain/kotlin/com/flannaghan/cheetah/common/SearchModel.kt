package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.datasource.DefinitionDataSource
import com.flannaghan.cheetah.common.datasource.dataSources
import com.flannaghan.cheetah.common.datasource.getAllWords
import com.flannaghan.cheetah.common.search.SearchContext
import com.flannaghan.cheetah.common.search.SearchResult
import com.flannaghan.cheetah.common.search.search
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.*

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

    val dataSources = dataSources(context)

    // State initialised by the asyncInit function.
    private var allWords: List<Word> = emptyList()
    private var searchContext: SearchContext? = null
    private var currentWordListDataSources: Set<DataSource> = emptySet()

    private val asyncInitialisation = scope.async {
        updateWordListDataSources(dataSources.filter { it.defaults.useWordList }.toSet())
        withContext(backgroundContext()) {
            allWords = getAllWords(dataSources, context)
        }
    }

    private suspend fun getSearchContext(wordListDataSources: Set<DataSource>): SearchContext {
        asyncInitialisation.await()
        val currentSearchContext = searchContext
        if (wordListDataSources != currentWordListDataSources || currentSearchContext == null) {
            val thisSearchContext = SearchContext(
                allWords.filter { word -> word.dataSources.any { it in wordListDataSources } },
                dataSources.filter { it.defaults.useDefinitions }.filterIsInstance<DefinitionDataSource>().firstOrNull()
                    ?.let {
                        { query, words -> it.fullTextSearch(context, words, query) }
                    }
            )
            currentWordListDataSources = wordListDataSources
            searchContext = thisSearchContext
            return thisSearchContext
        } else {
            return currentSearchContext
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
                dataSources
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