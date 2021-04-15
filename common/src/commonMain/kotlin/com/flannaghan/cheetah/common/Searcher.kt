package com.flannaghan.cheetah.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordSource
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.CoroutineContext

data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)

class Searcher(
    private val scope: CoroutineScope,
    private val backgroundContext: CoroutineContext,
    private val wordSource: WordSource,
) {
    val currentQuery = mutableStateOf<String?>(null)
    val resultState = mutableStateOf<SearchResult?>(null)
    private val lock = ReentrantLock()
    private var currentSearch: Job? = null

    init {
        startSearch(".*")
    }

    fun startSearch(query: String) {
        lock.withLock {
            if (query == currentQuery.value) return
            currentQuery.value = query
            currentSearch?.cancel()
            currentSearch = scope.launch {
                doSearch(query)
            }
        }
    }

    suspend fun doSearch(query: String) {
        val result = mutableListOf<Word>()
        try {
            withContext(backgroundContext) {
                val words = wordSource.words
                yield()
                val regex = Regex(query.toUpperCase(Locale.ROOT))

                for (wordChunk in words.chunked(100)) {
                    result.addAll(wordChunk.filter { regex.matches(it.entry) })
                    yield()
                }
            }
            yield()
            resultState.value = SearchResult(true, query, result)
        } catch (e: Exception) {
            resultState.value = SearchResult(false, query, listOf())
        }
    }
}