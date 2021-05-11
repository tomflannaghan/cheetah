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
data class Anagram(val letterCounts: Map<Char, Int>, val numberOfDots: Int) : Component()
object Dot : Component()

fun parseCustomPattern(string: String): CustomPattern {
    val letterCounts = mutableMapOf<Char, Int>()
    var dotCount = 0
    val components = mutableListOf<Component>()
    var inAnagram = false
    for (char in string) {
        if (inAnagram) {
            when (char) {
                '/' -> {
                    inAnagram = false
                    components.add(Anagram(letterCounts.toMap(), dotCount))
                    letterCounts.clear()
                    dotCount = 0
                }
                '.' -> dotCount++
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
        components.add(Anagram(letterCounts, dotCount))
    }
    return CustomPattern(components)
}

private data class State(
    var stringPosition: Int,
    var patternPosition: Int,
    var anagramState: AnagramState?
)

private data class AnagramState(
    val remainingLetterCounts: MutableMap<Char, Int>,
    var numberOfDots: Int,
)


enum class ComponentMatch { NO_MATCH, COMPLETE, PARTIAL }

private fun matchLetter(component: Component, c: Char, state: State): ComponentMatch {
    return when (component) {
        is Letter -> if (component.letter == c) ComponentMatch.COMPLETE else ComponentMatch.NO_MATCH
        is Anagram -> {
            if (state.anagramState == null) {
                state.anagramState = AnagramState(component.letterCounts.toMutableMap(), component.numberOfDots)
            }
            matchLetterAnagram(c, state.anagramState!!)
        }
        Dot -> ComponentMatch.COMPLETE
    }
}

/**
 * Matches against c, modifying anagram state as appropriate so that if we decide to continue the match, no
 * other state changes are required.
 */
private fun matchLetterAnagram(c: Char, anagramState: AnagramState): ComponentMatch {
    val currentValue = anagramState.remainingLetterCounts[c]
    var match = false
    if (currentValue != null && currentValue != 0) {
        // Consume the letter.
        anagramState.remainingLetterCounts[c] = currentValue - 1
        match = true
    } else if (anagramState.numberOfDots > 0) {
        // Can we use up a dot?
        anagramState.numberOfDots -= 1
        match = true
    }
    // Now are we complete or partially done?
    val done = anagramState.remainingLetterCounts.values.sum() == 0 && anagramState.numberOfDots == 0
    return when {
        !match -> ComponentMatch.NO_MATCH
        done -> ComponentMatch.COMPLETE
        else -> ComponentMatch.PARTIAL
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