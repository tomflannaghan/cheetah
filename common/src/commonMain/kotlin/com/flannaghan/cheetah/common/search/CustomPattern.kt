package com.flannaghan.cheetah.common.search

/**
 * Our custom pattern. Regex-like but cut down in some respects, and more fully featured in others.
 * - Anagrams
 */
data class CustomPattern(val components: List<Component>) {
    constructor(vararg components: Component) : this(components.asList())
}

sealed class Component

data class Letter(val letter: Char) : Component()
data class Anagram(val letterCounts: Map<Char, Int>) : Component()
object Dot : Component()

fun parseCustomPattern(string: String): CustomPattern {
    val letterCounts = mutableMapOf<Char, Int>()
    val components = mutableListOf<Component>()
    var inAnagram = false
    for (char in string) {
        if (inAnagram) {
            when (char) {
                '/' -> {
                    inAnagram = false
                    components.add(Anagram(letterCounts.toMap()))
                    letterCounts.clear()
                }
                '.' -> error("Can't have . inside an anagram")
                else -> letterCounts[char] = (letterCounts[char] ?: 0) + 1
            }
        } else {
            when (char) {
                '/' -> inAnagram = true
                '.' -> components.add(Dot)
                else -> components.add(Letter(char))
            }
        }
    }
    if (inAnagram) {
        components.add(Anagram(letterCounts))
    }
    return CustomPattern(components)
}

private data class State(
    var stringPosition: Int,
    var patternPosition: Int,
    var remainingLetterCounts: MutableMap<Char, Int>?
)

enum class ComponentMatch { NO_MATCH, COMPLETE, PARTIAL }

private fun matchLetter(component: Component, c: Char, state: State): ComponentMatch {
    return when (component) {
        is Letter -> if (component.letter == c) ComponentMatch.COMPLETE else ComponentMatch.NO_MATCH
        is Anagram -> {
            if (state.remainingLetterCounts == null) {
                state.remainingLetterCounts = component.letterCounts.toMutableMap()
            }
            require(state.remainingLetterCounts != null)
            if ((state.remainingLetterCounts!![c] ?: 0) > 0) {
                val currentCount = state.remainingLetterCounts!![c]!!
                if (currentCount == 1) state.remainingLetterCounts!! -= c
                else state.remainingLetterCounts!![c] = currentCount - 1
                // If the map's now empty we are done.
                if (state.remainingLetterCounts!!.isEmpty()) {
                    state.remainingLetterCounts = null
                    ComponentMatch.COMPLETE
                } else ComponentMatch.PARTIAL
            } else ComponentMatch.NO_MATCH
        }
        Dot -> ComponentMatch.COMPLETE
    }
}


class CustomPatternEvaluator(private val pattern: CustomPattern) {
    private fun match(chars: List<Char>): Boolean {
        val checkpoints = ArrayDeque<State>()
        checkpoints.addLast(State(0, 0, null))
        while (checkpoints.size > 0) {
            val current = checkpoints.last()
            // If we simultaneously reach the end of both, we're done. But if we reach either
            // end without the other, return false.
            if (current.patternPosition == pattern.components.size) {
                return current.stringPosition == chars.size
            } else if (current.stringPosition == chars.size) {
                return false
            }
            // Otherwise, we try to consume the next char and pattern.
            val char = chars[current.stringPosition]
            val component = pattern.components[current.patternPosition]
            when (matchLetter(component, char, current)) {
                ComponentMatch.NO_MATCH -> checkpoints.removeLast()
                ComponentMatch.COMPLETE -> {
                    current.patternPosition += 1
                    current.stringPosition += 1
                }
                ComponentMatch.PARTIAL -> {
                    current.stringPosition += 1
                }
            }
        }
        return false
    }

    fun match(string: String) = match(string.toList())
}