package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.search.custompattern.PrefixSearchNode
import com.flannaghan.cheetah.common.search.custompattern.prefixSearchTree
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Provides a context for sharing info between searches.
 */
class SearchContext(
    val words: List<Word>,
    val fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
) {
    private val searchTrees = DefaultDeferredMap<Boolean, PrefixSearchNode> { backwards ->
        val words = if (backwards) words.map { it.entry.reversed() } else words.map { it.entry }
        prefixSearchTree(words)
    }

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

    suspend fun getPrefixSearchTree(backwards: Boolean): PrefixSearchNode = coroutineScope {
        searchTrees.get(backwards)
    }

    suspend fun getPrefixSearchTreeForMatcher(matcher: Matcher, backwards: Boolean): PrefixSearchNode {
        val evaluation = evaluationContext ?: error("No evaluation available")
        return evaluation.getPrefixSearchTreeForMatcher(matcher, backwards)
    }
}


/**
 * Provides context for sharing info inside a particular search. The context exists for the lifetime of the
 * outermost search (i.e. the full search) and is available within any sub-searches.
 */
internal class SearchEvaluationContext(val context: SearchContext) {
    private var matcherSearchTrees =
        DefaultDeferredMap<Pair<Matcher, Boolean>, PrefixSearchNode> { (matcher, backwards) ->
            val words = matcher.matchingWords(context)
            val strings = if (backwards) words.map { it.entry.reversed() } else words.map { it.entry }
            prefixSearchTree(strings)
        }

    suspend fun getPrefixSearchTreeForMatcher(matcher: Matcher, backwards: Boolean): PrefixSearchNode {
        return matcherSearchTrees.get(Pair(matcher, backwards))
    }
}


internal class DefaultDeferredMap<K, V>(private val getValue: suspend (K) -> V) {
    private var _data = mutableMapOf<K, Deferred<V>>()

    // This class is safe for insertion into the lazy map.
    private val lock = Mutex(false)

    suspend fun get(key: K): V = coroutineScope {
        getNonCancelledAsync(key)?.let { return@coroutineScope it.await() }
        lock.withLock {
            val deferred = getNonCancelledAsync(key) ?: async {
                getValue(key)
            }
            _data[key] = deferred
            deferred
        }.await()
    }

    private fun getNonCancelledAsync(key: K): Deferred<V>? {
        val deferred = _data[key]
        return if (deferred == null || deferred.isCancelled) null else deferred
    }
}