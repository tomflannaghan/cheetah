package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word

/**
 * Provides a context for sharing info between searches.
 */
class SearchContext(
    val words: List<Word>,
    val fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
) {
    private var _prefixSearchTree: PrefixSearchNode? = null
    private var _reversePrefixSearchTree: PrefixSearchNode? = null

    val prefixSearchTree
        get(): PrefixSearchNode {
            val tree = _prefixSearchTree ?: prefixSearchTree(words.map { it.entry })
            _prefixSearchTree = tree
            return tree
        }
    val reversePrefixSearchTree
        get(): PrefixSearchNode {
            val tree = _reversePrefixSearchTree ?: prefixSearchTree(words.map { it.entry.reversed() })
            _reversePrefixSearchTree = tree
            return tree
        }
}