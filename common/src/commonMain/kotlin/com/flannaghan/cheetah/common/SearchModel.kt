package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordSource
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
    val allWords: List<Word>
        get() {
            val result = _allWords ?: wordSources.flatMap { it.words }.toSet().toList()
            _allWords = result
            return result
        }

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)

    private var currentJob: Job? = null
    private var currentJobQuery: String? = null

    suspend fun doSearch(query: String) = coroutineScope {
        if (query == currentJobQuery) return@coroutineScope
        // Throw out the old job.
        currentJob?.cancel()
        currentJob = launch {
            val newResult = withContext(backgroundContext()) {
                search(allWords, query)
            }
            updateResult(newResult)
        }
    }
}