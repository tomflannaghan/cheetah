package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.search.custompattern.CustomPattern
import com.flannaghan.cheetah.common.search.custompattern.CustomPatternEvaluator
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.regex.Pattern

/**
 * Pattern matches a given word. Works entirely with the entry form of the word, returning a boolean.
 */
sealed class Matcher {
    abstract suspend fun match(context: SearchContext, words: List<Word> = context.words): List<Boolean>
    fun <T> visit(block: (Matcher) -> T): List<T> = listOf(block(this)) + children.map { block(it) }
    abstract val children: List<Matcher>

    suspend fun matchingWords(context: SearchContext, words: List<Word> = context.words) =
        match(context, words).zip(words).filter { it.first }.map { it.second }
}

/**
 * Standard regex matching.
 */
data class RegexMatcher(val pattern: String) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        val matcher = Pattern.compile(pattern).matcher("")
        return words.map {
            matcher.reset(it.entry)
            matcher.matches()
        }
    }

    override val children = emptyList<Matcher>()
}

/**
 * Prefix matching.
 */
data class PrefixMatcher(val prefix: String) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> =
        words.map { it.entry.startsWith(prefix) }

    override val children = emptyList<Matcher>()
}

/**
 * Length matching.
 */
data class LengthMatcher(val min: Int?, val max: Int?) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> =
        words.map {
            (min == null || it.entry.length >= min) && (max == null || it.entry.length <= max)
        }

    override val children = emptyList<Matcher>()
}

/**
 * Processes matches in chunks asynchronously.
 */
data class ParallelChunkMatcher(val matcher: Matcher, private val chunkSize: Int = 10000) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> = coroutineScope {
        words
            .chunked(chunkSize)
            .map { chunk -> async { matcher.match(context, chunk) } }
            .awaitAll().flatten()
    }

    override val children = listOf(matcher)
}


/**
 * Full text search using the sqlite WordDatabase schema.
 */
data class FullTextSearchMatcher(private val matchQuery: String) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        if (words.isEmpty()) return emptyList()
        val results = context.fullTextSearch?.invoke(matchQuery, words)?.toSet() ?: error("No FTS found")
        return words.map { it in results }
    }

    override val children = emptyList<Matcher>()
}


/**
 * Relationship search using the sqlite WordDatabase schema.
 */
data class RelationshipMatcher(private val query: String) : Matcher() {
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        if (words.isEmpty()) return emptyList()
        val results = context.relationshipSearch?.invoke(query, words)?.toSet() ?: error("No relationship search found")
        return words.map { it in results }
    }

    override val children = emptyList<Matcher>()
}


/**
 * Our custom pattern matcher.
 */
data class CustomPatternMatcher(private val pattern: CustomPattern) : Matcher() {
    override val children = emptyList<Matcher>()
    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        val evaluator = CustomPatternEvaluator(pattern)
        return words.map { evaluator.match(context, it.entry) }
    }
}

/**
 * Applies an AND operator to the children. Stops trying a word after the first failure.
 */
data class AndMatcher(override val children: List<Matcher>) : Matcher() {
    constructor(vararg children: Matcher) : this(children.asList())

    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        val wordToIndex = words.withIndex().associateBy({ it.value }, { it.index })
        var remainingWords = words
        for (matcher in children) {
            remainingWords = matcher.matchingWords(context, remainingWords)
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
    constructor(vararg children: Matcher) : this(children.asList())

    override suspend fun match(context: SearchContext, words: List<Word>): List<Boolean> {
        val matches = MutableList(words.size) { false }
        val wordToIndex = words.withIndex().associateBy({ it.value }, { it.index })
        var remainingWords = words
        for (matcher in children) {
            val newRemaining = mutableListOf<Word>()
            for ((match, word) in matcher.match(context, remainingWords).zip(remainingWords)) {
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
    var dbSearch = false
    matcher.visit {
        if (it is ParallelChunkMatcher) parallelized = true
        if (it is FullTextSearchMatcher || it is RelationshipMatcher) dbSearch = true
    }
    return when {
        parallelized -> matcher
        dbSearch -> when (matcher) {
            is AndMatcher -> AndMatcher(matcher.children.map { parallelize(it) })
            is OrMatcher -> OrMatcher(matcher.children.map { parallelize(it) })
            else -> matcher
        }

        else -> ParallelChunkMatcher(matcher)
    }
}

/**
 * Optimise AND and OR ordering.
 * 1: Put the full text search first in AND. It is independent of the input word list.
 * 2: Put regexes in decreasing order of the number of letters for AND, and increasing for OR, to maximise drop out.
 * 3. Put any custom patterns last. They are much slower than regexes.
 * Also removes any duplicates.
 */
private fun optimizeOrdering(matcher: Matcher): Matcher = when (matcher) {
    is ParallelChunkMatcher -> ParallelChunkMatcher(optimizeOrdering(matcher.matcher))
    is AndMatcher -> AndMatcher(reorderChildrenDescending(matcher.children, ::andWeight))
    is OrMatcher -> OrMatcher(reorderChildrenDescending(matcher.children, ::orWeight))
    else -> matcher
}

private fun reorderChildrenDescending(matchers: List<Matcher>, weightFunc: (Matcher) -> Double) = matchers
    .map { optimizeOrdering(it) }
    .distinct()
    .sortedByDescending { weightFunc(it) }

private fun andWeight(matcher: Matcher): Double = when (matcher) {
    is PrefixMatcher -> matcher.prefix.length * 5.0
    is RegexMatcher -> matcher.pattern.count { it in 'a'..'z' || it in 'A'..'Z' } * 1.0
    is LengthMatcher -> 100.0
    is FullTextSearchMatcher -> 1000.0
    is RelationshipMatcher -> 1000.0
    is CustomPatternMatcher -> -1000.0
    else -> matcher.children.sumOf { andWeight(it) }
}

private fun orWeight(matcher: Matcher): Double = when (matcher) {
    is PrefixMatcher -> matcher.prefix.length * -1.0
    is RegexMatcher -> matcher.pattern.count { it in 'a'..'z' || it in 'A'..'Z' } * -5.0
    is LengthMatcher -> 100.0
    is FullTextSearchMatcher -> 1000.0
    is RelationshipMatcher -> 1000.0
    is CustomPatternMatcher -> -1000.0
    else -> matcher.children.sumOf { orWeight(it) }
}