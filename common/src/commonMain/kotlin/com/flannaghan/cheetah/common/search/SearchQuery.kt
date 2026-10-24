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
 * A search for phrases of [num] words.
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

private val PREFIX_REGEX = Regex("[A-Z]+")
private val LENGTH_REGEX = Regex("\\d*-?\\d*")
private val NUM_WORDS_REGEX = Regex("W:\\d+")

/**
 * Splits a query string into its uppercased terms, which are separated by newlines or `;`.
 */
private fun terms(string: String): List<String> = string.uppercase(Locale.ROOT)
    .lines()
    .flatMap { it.split(';') }
    .map { it.trim() }
    .filter { it.isNotEmpty() }

/**
 * The search terms of the full text search (`s:`) terms in a query string, uppercased.
 */
fun fullTextSearchTerms(string: String): List<String> =
    terms(string).filter { it.startsWith("S:") }.map { it.substring(2).trim() }

/**
 * Given a string, split into lines and parse each one, producing a search query. Lines are ANDed together.
 */
fun stringToSearchQuery(string: String): SearchQuery {
    val queries = mutableListOf<SearchQuery>()
    for (term in terms(string)) {
        when {
            // These are all combined into a single query below.
            term.startsWith("S:") -> {}

            term.startsWith("R:") -> queries.add(RelationshipSearchQuery(term.substring(2).trim()))

            "/`<>".any { it in term } -> queries.add(CustomPatternSearchQuery(term))

            term.matches(PREFIX_REGEX) -> queries.add(PrefixSearchQuery(term))

            term.matches(LENGTH_REGEX) -> {
                if ("-" in term) {
                    val (min, max) = term.split("-", limit = 2).map { if (it.isEmpty()) null else it.toInt() }
                    queries.add(LengthSearchQuery(min, max))
                } else {
                    val len = term.toInt()
                    queries.add(LengthSearchQuery(len, len))
                }
            }

            term.matches(NUM_WORDS_REGEX) -> queries.add(NumWordsSearchQuery(term.substring(2).toInt()))

            else -> queries.add(RegexSearchQuery(term))
        }
    }
    val fullTextTerms = fullTextSearchTerms(string)
    if (fullTextTerms.isNotEmpty()) {
        queries.add(FullTextSearchQuery(fullTextTerms.joinToString(" AND ") { "\"$it\"" }))
    }
    return queries.singleOrNull() ?: AndSearchQuery(queries)
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