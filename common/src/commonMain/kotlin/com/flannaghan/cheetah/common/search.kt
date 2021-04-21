package com.flannaghan.cheetah.common

import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordSource
import kotlinx.coroutines.yield
import java.util.*

data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)

suspend fun search(wordSources: List<WordSource>, query: String): SearchResult {
    val matches = mutableListOf<Word>()
    var success = false
    try {
        val words = wordSources.flatMap { it.words }.toSet().toList()
        yield()

        val regex = Regex(query.toUpperCase(Locale.ROOT))
        for (wordChunk in words.chunked(1000)) {
            matches.addAll(wordChunk.filter { regex.matches(it.entry) })
            yield()
        }
        yield()
        success = true

    } catch (e: Exception) {
        matches.clear()
    }
    return SearchResult(success, query, matches)
}
