package com.flannaghan.cheetah.common

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.*

data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)

suspend fun search(words: List<Word>, query: String): SearchResult = coroutineScope {
    val matches = mutableListOf<Word>()
    var success = false
    try {
        val regex = Regex(query.toUpperCase(Locale.ROOT))
        words.chunked(1000).map {
            async { it.filter { word -> regex.matches(word.entry) } }
        }.awaitAll().forEach {
            matches.addAll(it)
        }

        success = true

    } catch (e: Exception) {
        matches.clear()
    }

    SearchResult(success, query, matches)
}
