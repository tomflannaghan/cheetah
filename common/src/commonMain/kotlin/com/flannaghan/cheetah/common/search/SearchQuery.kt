package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import java.util.*

sealed class SearchQuery

/**
 * A full text search. [matchPattern] is the string passed as the MATCH ... argument to sqlite.
 */
data class FullTextSearchQuery(val matchPattern: String) : SearchQuery()

/**
 * A regex query.
 */
data class RegexSearchQuery(val pattern: String) : SearchQuery()

/**
 * A pattern match.
 */
data class CustomPatternSearchQuery(val pattern: String) : SearchQuery()

/**
 * The and operator. Matches in order, stopping at the first false match.
 */
data class AndSearchQuery(val children: List<SearchQuery>) : SearchQuery() {
    constructor(vararg children: SearchQuery) : this(children.asList())
}

/**
 * Given a string, split into lines and parse each one, producing a search query. Lines are ANDed together.
 */
fun stringToSearchQuery(string: String): SearchQuery {
    val fullTextTerms = mutableListOf<String>()
    val queries = mutableListOf<SearchQuery>()
    for (line in string.lines()) {
        when {
            line.startsWith("s:") -> fullTextTerms.add(line.substring(2))
            '/' in line || '`' in line -> queries.add(CustomPatternSearchQuery(line.toUpperCase(Locale.ROOT)))
            else -> queries.add(RegexSearchQuery(line.toUpperCase(Locale.ROOT)))
        }
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
        is CustomPatternSearchQuery -> CustomPatternMatcher(parseCustomPattern(query.pattern))
    }
}