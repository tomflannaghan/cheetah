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
        val patterns = query.lines().map { Regex(it.toUpperCase(Locale.ROOT)).toPattern() }
        words.chunked(10000).map { chunk ->
            async {
                val result = mutableListOf<Word>()
                // Matcher isn't threadsafe so make one for each chunk.
                val matchers = patterns.map { it.matcher("") }
                for (word in chunk) {
                    if (matchers.all {
                            it.reset(word.entry)
                            it.matches()
                        }) {
                        result.add(word)
                    }
                }
                result
            }
        }.awaitAll().forEach {
            matches.addAll(it)
        }
        // We succeeded - mark as such.
        success = true
    } catch (e: Exception) {
        // Reset matches. In case of error we report no matches.
        matches.clear()
    }

    SearchResult(success, query, matches)
}
