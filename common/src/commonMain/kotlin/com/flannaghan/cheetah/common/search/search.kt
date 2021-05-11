package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.coroutineScope


suspend fun search(
    query: String,
    context: SearchContext
): SearchResult = coroutineScope {
    var matches: List<Word>
    var success = false
    try {
        val matcher = optimize(searchQueryToMatcher(stringToSearchQuery(query)))
        matches = matcher.matchingWords(context)
        // We succeeded - mark as such.
        success = true
    } catch (e: Exception) {
        // Reset matches. In case of error we report no matches.
        println(e)
        matches = emptyList()
    }

    SearchResult(success, query, matches)
}


data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)