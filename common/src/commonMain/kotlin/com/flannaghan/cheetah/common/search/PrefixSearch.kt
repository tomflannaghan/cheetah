package com.flannaghan.cheetah.common.search

data class PrefixSearchNode(private val children: Map<Char, PrefixSearchNode>, val isWord: Boolean) {
    fun matches(c: Char) = c in children
    fun descend(c: Char): PrefixSearchNode = children[c] ?: error("No node at $c")
    val nextLetters get() = children.keys
}

fun prefixSearchTree(strings: List<String>): PrefixSearchNode {
    val (emptyStrings, nonEmptyStrings) = strings.partition { it.isEmpty() }
    val wordsByFirstLetter = nonEmptyStrings.groupBy({ it.first() }, { it.substring(1) })
    return PrefixSearchNode(wordsByFirstLetter.mapValues { prefixSearchTree(it.value) }, emptyStrings.isNotEmpty())
}