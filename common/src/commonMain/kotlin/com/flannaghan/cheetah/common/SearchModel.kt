package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.datasource.dataSources
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.*

abstract class SearchModel(private val context: ApplicationContext) {
    @Composable
    abstract fun queryState(): State<String>

    @Composable
    abstract fun resultState(): State<SearchResult>

    @Composable
    abstract fun definitionState(): State<String>

    val dataSources = dataSources(context)

    private var _allWords: List<Word>? = null

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)
    abstract fun updateDefinition(definition: String)

    private var currentJobQuery: String? = null

    val searchLauncher = SingleJobLauncher()
    val definitionLookupLauncher = SingleJobLauncher()

    suspend fun getAllWords(): List<Word> = coroutineScope {
        // Return if already populated.
        _allWords?.let { return@coroutineScope it }
        // Otherwise populate in parallel.
        val allWords = dataSources
            .map { async { it.wordList.getWords() } }
            .awaitAll()
            .flatten()
            .toSet()
            .sortedBy { it.string }
        _allWords = allWords
        return@coroutineScope allWords
    }

    suspend fun doSearch(query: String) = coroutineScope {
        if (query == currentJobQuery) return@coroutineScope
        searchLauncher.launch(this) {
            val newResult = withContext(backgroundContext()) {
                search(getAllWords(), query)
            }
            updateResult(newResult)
        }
    }

    suspend fun lookupDefinition(word: Word) = coroutineScope {
        definitionLookupLauncher.launch(this) {
            val definitions = withContext(backgroundContext()) {
                dataSources
                    .map { it.definitionSearcher }
                    .filterNotNull()
                    .map { async { it.lookupDefinition(word) } }
                    .awaitAll()
            }
            updateDefinition(definitions.joinToString("\n\n"))
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