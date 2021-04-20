package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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

    abstract fun updateQuery(query: String)
    abstract fun updateResult(result: SearchResult)

    private var currentJob: Job? = null
    private var currentJobQuery: String? = null

    suspend fun doSearch(query: String) = coroutineScope {
        if (query == currentJobQuery) return@coroutineScope
        // Throw out the old job.
        currentJob?.cancel()

        updateQuery(query)
        currentJob = launch {
            val newResult = withContext(backgroundContext()) {
                search(wordSources, query)
            }
            updateResult(newResult)
        }
    }
}