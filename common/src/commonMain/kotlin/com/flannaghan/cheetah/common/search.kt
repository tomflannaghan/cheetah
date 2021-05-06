package com.flannaghan.cheetah.common

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.*
import java.util.regex.Pattern

data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)

suspend fun search(
    words: List<Word>,
    query: String,
    fts: (suspend (String, List<Word>) -> List<Word>)? = null
): SearchResult = coroutineScope {
    val matches = mutableListOf<Word>()
    var success = false
    try {
        val patterns = mutableListOf<Pattern>()
        val fullTextSearchTerms = mutableListOf<String>()
        for (queryLine in query.lines()) {
            if (queryLine.startsWith("s:")) {
                fullTextSearchTerms.add(queryLine.substring(2))
            } else {
                patterns.add(Regex(queryLine.toUpperCase(Locale.ROOT)).toPattern())
            }
        }

        val filteredWords = if (fullTextSearchTerms.isNotEmpty() && fts != null) {
            fts(fullTextSearchTerms.joinToString(" AND ") { "\"$it\"" }, words)
        } else words

        filteredWords.chunked(10000).map { chunk ->
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
