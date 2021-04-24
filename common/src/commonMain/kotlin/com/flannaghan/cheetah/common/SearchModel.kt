package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.db.WordDatabase
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordSource
import com.flannaghan.cheetah.common.words.stringsToWordsParallel
import kotlinx.coroutines.*

abstract class SearchModel {
    @Composable
    abstract fun queryState(): State<String>

    @Composable
    abstract fun resultState(): State<SearchResult>

    @Composable
    abstract fun definitionState(): State<String>

    var wordSources: List<WordSource> = listOf()
    private var _allWords: List<Word>? = null

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)
    abstract fun updateDefinition(definition: String)

    abstract fun getDatabase(): WordDatabase

    private var currentJobQuery: String? = null

    val searchLauncher = SingleJobLauncher()
    val definitionLookupLauncher = SingleJobLauncher()

    suspend fun getAllWords(): List<Word> = coroutineScope {
        // Return if already populated.
        _allWords?.let { return@coroutineScope it }
        // Otherwise populate in parallel.
        val allWordStrings = wordSources.flatMap { it.words }.toSet()
        val allWords = stringsToWordsParallel(allWordStrings)
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
            withContext(backgroundContext()) {
                val db = getDatabase()
                println("Searching for ${word.entry}")
                // TODO make this coroutiney.
                val definitions = db.definitionQueries.definitionForWord(word.entry).executeAsList()
                println("Found $definitions")
                updateDefinition(definitions?.joinToString("\n\n") { it.definition } ?: "")
            }
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