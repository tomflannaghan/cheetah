package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import java.util.*
import java.util.regex.Pattern

sealed class SearchQuery

/**
 * A full text search. [matchPattern] is the string passed as the MATCH ... argument to sqlite.
 */
data class FullTextSearchQuery(val matchPattern: String) : SearchQuery()

/**
 * A regex query.
 */
data class RegexSearchQuery(val pattern: Pattern) : SearchQuery()

/**
 * The and operator. Matches in order, stopping at the first false match.
 */
data class AndSearchQuery(val children: List<SearchQuery>) : SearchQuery()

/**
 * Given a string, split into lines and parse each one, producing a search query. Lines are ANDed together.
 */
fun stringToSearchQuery(string: String): SearchQuery {
    val fullTextTerms = mutableListOf<String>()
    val queries = mutableListOf<SearchQuery>()
    for (line in string.lines()) {
        if (line.startsWith("s:")) fullTextTerms.add(line.substring(2))
        else queries.add(RegexSearchQuery(Pattern.compile(line.toUpperCase(Locale.ROOT))))
    }
    if (fullTextTerms.isNotEmpty()) {
        queries.add(FullTextSearchQuery(fullTextTerms.joinToString(" AND ") { "\"$it\"" }))
    }
    return if (queries.size == 1) queries[0] else AndSearchQuery(queries)
}

/**
 * Construct a matcher from a search query.
 */
fun searchQueryToMatcher(
    query: SearchQuery,
    fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
): Matcher {
    return when (query) {
        is FullTextSearchQuery -> FullTextSearchMatcher(
            fullTextSearch ?: error("No full text search provider found"),
            query.matchPattern
        )
        is RegexSearchQuery -> RegexMatcher(query.pattern)
        is AndSearchQuery -> AndMatcher(query.children.map { searchQueryToMatcher(it, fullTextSearch) })
    }
}