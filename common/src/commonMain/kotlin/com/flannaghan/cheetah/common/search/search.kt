package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.coroutineScope


suspend fun search(
    words: List<Word>,
    query: String,
    fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
): SearchResult = coroutineScope {
    var matches: List<Word>
    var success = false
    try {
        val matcher = optimize(searchQueryToMatcher(stringToSearchQuery(query), fullTextSearch))
        matches = matcher.match(words).zip(words).filter { it.first }.map { it.second }
        // We succeeded - mark as such.
        success = true
    } catch (e: Exception) {
        // Reset matches. In case of error we report no matches.
        matches = emptyList()
    }

    SearchResult(success, query, matches)
}


data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)