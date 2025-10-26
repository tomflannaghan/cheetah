package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.search.custompattern.parseCustomPattern
import java.util.*

sealed class SearchQuery

/**
 * A full text search. [matchPattern] is the string passed as the MATCH ... argument to sqlite.
 */
data class FullTextSearchQuery(val matchPattern: String) : SearchQuery()

/**
 * A relationship search. [query] is a root word, and the search will return related words.
 */
data class RelationshipSearchQuery(val query: String) : SearchQuery()

/**
 * A regex query.
 */
data class RegexSearchQuery(val pattern: String) : SearchQuery()

/**
 * A prefix search.
 */
data class PrefixSearchQuery(val prefix: String) : SearchQuery()

/**
 * A length search.
 */
data class LengthSearchQuery(val min: Int?, val max: Int?) : SearchQuery()

/**
 * A length search.
 */
data class NumWordsSearchQuery(val num: Int) : SearchQuery()

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
        for (term in line.toUpperCase(Locale.ROOT).split(';')) {
            if (term.trim().isEmpty()) continue
            when {
                term.startsWith("S:") -> fullTextTerms.add(term.substring(2))

                term.startsWith("R:") -> queries.add(RelationshipSearchQuery(term.substring(2)))

                "/`<>".any { it in term } -> queries.add(CustomPatternSearchQuery(term))

                term.matches(Regex("^[A-Z]+$")) -> queries.add(PrefixSearchQuery(term))

                term.isNotEmpty() && term.matches(Regex("^\\d*-?\\d*$")) -> {
                    if ("-" in term) {
                        val (min, max) = term.split("-", limit = 2).map {
                            if (it == "") null else it.toInt()
                        }
                        queries.add(LengthSearchQuery(min, max))
                    } else {
                        val len = term.toInt()
                        queries.add(LengthSearchQuery(len, len))
                    }
                }

                term.isNotEmpty() && term.matches(Regex("^W:\\d+$")) -> {
                    queries.add(NumWordsSearchQuery(term.substring(2).toInt()))
                }

                else -> queries.add(RegexSearchQuery(term))
            }
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
fun searchQueryToMatcher(query: SearchQuery): Matcher {
    return when (query) {
        is FullTextSearchQuery -> FullTextSearchMatcher(query.matchPattern)
        is RelationshipSearchQuery -> RelationshipMatcher(query.query)
        is RegexSearchQuery -> RegexMatcher(query.pattern)
        is PrefixSearchQuery -> PrefixMatcher(query.prefix)
        is LengthSearchQuery -> LengthMatcher(query.min, query.max)
        is NumWordsSearchQuery -> NumWordsMatcher(query.num)
        is AndSearchQuery -> AndMatcher(query.children.map { searchQueryToMatcher(it) })
        is CustomPatternSearchQuery -> CustomPatternMatcher(parseCustomPattern(query.pattern))
    }
}