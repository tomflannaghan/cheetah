package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.datasource.dataSources
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

    private val dataSources = dataSources(context)

    private val allWordsDeferred = scope.async { populateAllWords() }
    private val searchContextDeferred = scope.async {
        SearchContext(
            getAllWords(),
            dataSources.firstOrNull { it.definitionSearcher != null }
                ?.definitionSearcher?.let {
                    { query, words -> it.fullTextSearch(context, words, query) }
                }
        )
    }

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)
    abstract fun updateDefinition(definition: String)

    private var currentJobQuery: String? = null

    private val searchLauncher = SingleJobLauncher()
    private val definitionLookupLauncher = SingleJobLauncher()

    private suspend fun populateAllWords() = coroutineScope {
        val allWords = dataSources
            .filter { it.defaults.useWordList }
            .map { async { it.wordListFetcher.getWords(context) } }
            .awaitAll()
            .flatten()
            .distinct()
            .sortedBy { it.entry }
        allWords
    }

    private suspend fun getAllWords(): List<Word> = allWordsDeferred.await()

    suspend fun doSearch(query: String) = coroutineScope {
        if (query == currentJobQuery) return@coroutineScope
        searchLauncher.launch(this) {
            val newResult = withContext(backgroundContext()) {
                val searchContext = searchContextDeferred.await()
                searchContext.withEvaluation {
                    search(query, searchContext)
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
                    .mapNotNull { it.definitionSearcher }
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