package com.flannaghan.cheetah.common.search

data class PrefixSearchNode(
    val children: List<PrefixSearchNode>,
    val char: Char,
    val isWord: Boolean
)

fun prefixSearchTree(strings: List<String>) = prefixSearchTree(strings, ' ', 0)

private fun prefixSearchTree(strings: List<String>, char: Char, depth: Int): PrefixSearchNode {
    val stringsByFirstLetter = mutableMapOf<Char, MutableList<String>>()
    var isWord = false
    for (string in strings) {
        if (string.length > depth) {
            stringsByFirstLetter.getOrPut(string[depth]) { mutableListOf() }.add(string)
        } else if (string.length == depth) {
            isWord = true
        }
    }
    return PrefixSearchNode(
        stringsByFirstLetter.map { prefixSearchTree(it.value, it.key, depth + 1) },
        char, isWord
    )
}