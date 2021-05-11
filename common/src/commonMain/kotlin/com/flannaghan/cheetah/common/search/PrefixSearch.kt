package com.flannaghan.cheetah.common.search

data class PrefixSearchNode(private val children: Map<Char, PrefixSearchNode>, val isWord: Boolean) {
    fun matches(c: Char) = c in children
    fun descend(c: Char): PrefixSearchNode = children[c] ?: error("No node at $c")
    val nextLetters get() = children.keys
}

fun prefixSearchTree(strings: List<String>) = prefixSearchTree(strings, 0)

private fun prefixSearchTree(strings: List<String>, depth: Int): PrefixSearchNode {
    val stringsByFirstLetter = mutableMapOf<Char, MutableList<String>>()
    var isWord = false
    for (string in strings) {
        if (string.length > depth) {
            stringsByFirstLetter.getOrPut(string[depth]) { mutableListOf() }.add(string)
        } else if (string.length == depth) {
            isWord = true
        }
    }
    return PrefixSearchNode(stringsByFirstLetter.mapValues { prefixSearchTree(it.value, depth + 1) }, isWord)
}