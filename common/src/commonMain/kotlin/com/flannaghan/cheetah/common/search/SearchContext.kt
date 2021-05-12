package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.search.custompattern.PrefixSearchNode
import com.flannaghan.cheetah.common.search.custompattern.prefixSearchTree
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Provides a context for sharing info between searches.
 */
class SearchContext(
    val words: List<Word>,
    val fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
) {
    private var _searchTrees = mutableMapOf<Boolean, PrefixSearchNode>()

    // We request the prefix tree in multiple coroutines, so it's important that we wait until populated
    // otherwise we'll try to construct it in multiple threads simultaneously.
    private val lock = Mutex(false)

    // Certain things only need persisting for the evaluation, and should be discarded once done.
    private var evaluationContext: SearchEvaluationContext? = null
    private val evaluationLock = Mutex(false)

    suspend fun <R> withEvaluation(block: suspend () -> R): R {
        // We only allow a single top level evaluation to take place at any time.
        evaluationLock.withLock {
            evaluationContext = SearchEvaluationContext(this)
            try {
                return block()
            } finally {
                evaluationContext = null
            }
        }
    }

    suspend fun getPrefixSearchTree(backwards: Boolean): PrefixSearchNode {
        _searchTrees[backwards]?.let { return it }
        lock.withLock {
            val currentTree = _searchTrees[backwards]
            return if (currentTree != null) {
                currentTree
            } else {
                val words = if (backwards) words.map { it.entry.reversed() } else words.map { it.entry }
                val tree = prefixSearchTree(words)
                _searchTrees[backwards] = tree
                tree
            }
        }
    }

    suspend fun getPrefixSearchTreeForMatcher(matcher: Matcher, backwards: Boolean): PrefixSearchNode {
        val evaluation = evaluationContext ?: error("No evaluation available")
        return evaluation.getPrefixSearchTreeForMatcher(matcher, backwards)
    }
}


internal class SearchEvaluationContext(val context: SearchContext) {
    private var _matcherSearchTrees = mutableMapOf<Pair<Matcher, Boolean>, PrefixSearchNode>()

    // We request the prefix tree in multiple coroutines, so it's important that we wait until populated
    // otherwise we'll try to construct it in multiple threads simultaneously.
    private val lock = Mutex(false)

    suspend fun getPrefixSearchTreeForMatcher(matcher: Matcher, backwards: Boolean): PrefixSearchNode {
        val key = Pair(matcher, backwards)
        _matcherSearchTrees[key]?.let { return it }
        lock.withLock {
            val currentTree = _matcherSearchTrees[key]
            return if (currentTree != null) {
                currentTree
            } else {
                val words = matcher.matchingWords(context)
                val strings = if (backwards) words.map { it.entry.reversed() } else words.map { it.entry }
                val tree = prefixSearchTree(strings)
                _matcherSearchTrees[key] = tree
                tree
            }
        }
    }
}