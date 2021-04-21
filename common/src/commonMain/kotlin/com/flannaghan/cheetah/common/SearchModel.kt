package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordSource
import com.flannaghan.cheetah.common.words.stringsToWordsParallel
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class SearchModel {
    @Composable
    abstract fun queryState(): State<String>

    @Composable
    abstract fun resultState(): State<SearchResult>

    var wordSources: List<WordSource> = listOf()
    private var _allWords: List<Word>? = null

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)

    private var currentJob: Job? = null
    private var currentJobQuery: String? = null

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
        // Throw out the old job.
        currentJob?.cancel()
        currentJob = launch {
            val newResult = withContext(backgroundContext()) {
                search(getAllWords(), query)
            }
            updateResult(newResult)
        }
    }
}