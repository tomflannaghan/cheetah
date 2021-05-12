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
}