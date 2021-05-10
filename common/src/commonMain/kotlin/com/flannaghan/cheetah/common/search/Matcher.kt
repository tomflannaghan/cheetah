package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.regex.Pattern

/**
 * Pattern matches a given word. Works entirely with the entry form of the word, returning a boolean.
 */
sealed class Matcher {
    abstract suspend fun match(words: List<Word>): List<Boolean>
    fun <T> visit(block: (Matcher) -> T): List<T> = listOf(block(this)) + children.map { block(it) }
    abstract val children: List<Matcher>
}

/**
 * Standard regex matching.
 */
data class RegexMatcher(val pattern: Pattern) : Matcher() {
    override suspend fun match(words: List<Word>): List<Boolean> {
        val matcher = pattern.matcher("")
        return words.map {
            matcher.reset(it.entry)
            matcher.matches()
        }
    }

    override val children = emptyList<Matcher>()
}


/**
 * Processes matches in chunks asynchronously.
 */
data class ParallelChunkMatcher(private val matcher: Matcher, private val chunkSize: Int = 10000) : Matcher() {
    override suspend fun match(words: List<Word>): List<Boolean> = coroutineScope {
        words
            .chunked(chunkSize)
            .map { chunk -> async { matcher.match(chunk) } }
            .awaitAll().flatten()
    }

    override val children = listOf(matcher)
}


/**
 * Full text search using the sqlite WordDatabase schema.
 */
data class FullTextSearchMatcher(
    private val fullTextSearch: suspend (String, List<Word>) -> List<Word>,
    private val matchQuery: String
) : Matcher() {
    override suspend fun match(words: List<Word>): List<Boolean> {
        if (words.isEmpty()) return emptyList()
        val ftsResults = fullTextSearch(matchQuery, words).toSet()
        return words.map { it in ftsResults }
    }

    override val children = emptyList<Matcher>()
}

/**
 * Applies an AND operator to the children. Stops trying a word after the first failure.
 */
data class AndMatcher(override val children: List<Matcher>) : Matcher() {
    override suspend fun match(words: List<Word>): List<Boolean> {
        val wordToIndex = words.withIndex().associateBy({ it.value }, { it.index })
        var remainingWords = words
        for (matcher in children) {
            remainingWords = matcher.match(remainingWords).zip(remainingWords).filter { it.first }.map { it.second }
        }
        val matches = MutableList(words.size) { false }
        for (word in remainingWords) {
            matches[wordToIndex[word]!!] = true
        }
        return matches
    }
}

/**
 * Applies the OR operator. Stops trying a word after the first success.
 */
data class OrMatcher(override val children: List<Matcher>) : Matcher() {
    override suspend fun match(words: List<Word>): List<Boolean> {
        val matches = MutableList(words.size) { false }
        val wordToIndex = words.withIndex().associateBy({ it.value }, { it.index })
        var remainingWords = words
        for (matcher in children) {
            val newRemaining = mutableListOf<Word>()
            for ((match, word) in matcher.match(remainingWords).zip(remainingWords)) {
                if (match) matches[wordToIndex[word]!!] = true
                else newRemaining.add(word)
            }
            remainingWords = newRemaining
        }
        return matches
    }
}

/**
 * Apply optimizations to the matcher with the aim of increasing performance.
 */
fun optimize(matcher: Matcher): Matcher = optimizeOrdering(parallelize(matcher))

/**
 * If it makes sense, parallelize a matcher. Full text search is excluded from parallelization, as is anything
 * that already involves any amount of parallelization. The parallelism is put in at the highest level possible.
 */
private fun parallelize(matcher: Matcher): Matcher {
    var parallelized = false
    var fullTextSearch = false
    matcher.visit {
        if (it is ParallelChunkMatcher) parallelized = true
        if (it is FullTextSearchMatcher) fullTextSearch = true
    }
    return when {
        parallelized -> matcher
        fullTextSearch -> when (matcher) {
            is RegexMatcher -> matcher
            is ParallelChunkMatcher -> matcher
            is FullTextSearchMatcher -> matcher
            is AndMatcher -> AndMatcher(matcher.children.map { parallelize(it) })
            is OrMatcher -> OrMatcher(matcher.children.map { parallelize(it) })
        }
        else -> ParallelChunkMatcher(matcher)
    }
}

/**
 * Optimise AND and OR ordering.
 * 1: Put the full text search first in AND. It is independent of the input word list.
 * 2: Put regexes in decreasing order of the number of letters for AND, and increasing for OR, to maximise drop out.
 * Also removes any duplicates.
 */
private fun optimizeOrdering(matcher: Matcher): Matcher = when (matcher) {
    is RegexMatcher -> matcher
    is ParallelChunkMatcher -> matcher
    is FullTextSearchMatcher -> matcher
    is AndMatcher -> AndMatcher(reorderChildrenDescending(matcher.children))
    is OrMatcher -> OrMatcher(reorderChildrenDescending(matcher.children).asReversed())
}

private fun reorderChildrenDescending(matchers: List<Matcher>) = matchers
    .map { optimizeOrdering(it) }
    .distinct()
    .sortedByDescending { weight(it) }

private fun weight(matcher: Matcher): Double = when (matcher) {
    is RegexMatcher -> matcher.pattern.pattern().count { it in 'a'..'z' || it in 'A'..'Z' } * 1.0
    is FullTextSearchMatcher -> 1000.0
    else -> matcher.children.sumOf { weight(it) }
}