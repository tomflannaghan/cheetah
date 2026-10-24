package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.CancellationException

/**
 * Runs [query] against the words in [context]. An invalid query gives an unsuccessful result with no words.
 */
suspend fun search(query: String, context: SearchContext): SearchResult {
    return try {
        val matcher = optimize(searchQueryToMatcher(stringToSearchQuery(query)))
        SearchResult(true, query, matcher.matchingWords(context))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        println(e)
        SearchResult(false, query, emptyList())
    }
}

data class SearchResult(val success: Boolean, val query: String, val words: List<Word>)
