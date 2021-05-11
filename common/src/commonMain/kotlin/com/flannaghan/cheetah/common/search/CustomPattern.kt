package com.flannaghan.cheetah.common.search

/**
 * Our custom pattern. Regex-like but cut down in some respects, and more fully featured in others.
 * - Anagrams
 */
data class CustomPattern(val components: List<Component>, val misprints: Int = 0) {
    constructor(vararg components: Component, misprints: Int = 0) : this(components.asList(), misprints)
}

sealed class Component

data class Letter(val letter: Char) : Component()
data class Anagram(val letterCounts: Map<Char, Int>, val numberOfDots: Int) : Component()
object Dot : Component()

fun parseCustomPattern(string: String): CustomPattern {
    // Handle misprints.
    if (string.startsWith('`')) {
        val stringNoBackticks = string.trimStart('`')
        return parseCustomPattern(stringNoBackticks).copy(misprints = string.length - stringNoBackticks.length)
    }
    // Otherwise, parse with no misprints.
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
    var anagramState: AnagramState?,
    var misprints: Int,
)

private data class AnagramState(
    val remainingLetterCounts: MutableMap<Char, Int>,
    var numberOfDots: Int,
    var misprintsConsumed: Int,
)


private data class ComponentMatch(val complete: Boolean, val match: Boolean) {
    companion object {
        val COMPLETE = ComponentMatch(complete = true, match = true)
        val PARTIAL = ComponentMatch(complete = false, match = true)
        val NO_COMPLETE = ComponentMatch(complete = true, match = false)
        val NO_PARTIAL = ComponentMatch(complete = false, match = false)
    }
}


private fun matchLetter(component: Component, c: Char, state: State): ComponentMatch {
    return when (component) {
        is Letter -> if (component.letter == c) ComponentMatch.COMPLETE else ComponentMatch.NO_COMPLETE
        is Anagram -> {
            if (state.anagramState == null) {
                state.anagramState = AnagramState(
                    component.letterCounts.toMutableMap(), component.numberOfDots, 0
                )
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
    val match: Boolean
    if (currentValue != null && currentValue != 0) {
        // Consume the letter.
        anagramState.remainingLetterCounts[c] = currentValue - 1
        match = true
    } else if (anagramState.numberOfDots > 0) {
        // Can we use up a dot?
        anagramState.numberOfDots -= 1
        match = true
    } else {
        anagramState.misprintsConsumed++
        match = false
    }
    // Now are we complete or partially done? We're done if the number of letters remaining equals the misprints
    // this anagram has consumed.
    val charsRemaining = anagramState.remainingLetterCounts.values.sum() + anagramState.numberOfDots
    val done = charsRemaining == anagramState.misprintsConsumed
    return when {
        !match && done -> ComponentMatch.NO_COMPLETE
        !match && !done -> ComponentMatch.NO_PARTIAL
        match && done -> ComponentMatch.COMPLETE
        else -> ComponentMatch.PARTIAL
    }
}


class CustomPatternEvaluator(private val pattern: CustomPattern) {
    private fun match(chars: List<Char>): Boolean {
        val checkpoints = ArrayDeque<State>()
        checkpoints.addLast(State(0, 0, null, pattern.misprints))
        while (checkpoints.size > 0) {
            val current = checkpoints.last()
            // If we simultaneously reach the end of both, and we've used up all of the misprints, we're done.
            // But if we reach either end without the other, return false.
            if (current.patternPosition == pattern.components.size) {
                return current.stringPosition == chars.size && current.misprints == 0
            } else if (current.stringPosition == chars.size) {
                return false
            }
            // Otherwise, we try to consume the next char and pattern.
            val char = chars[current.stringPosition]
            val component = pattern.components[current.patternPosition]
            var result = matchLetter(component, char, current)
            if (!result.match && current.misprints > 0) {
                current.misprints--
                result = result.copy(match = true)
            }
            when {
                !result.match -> checkpoints.removeLast()
                result.complete -> {
                    current.patternPosition += 1
                    current.stringPosition += 1
                }
                else -> {
                    current.stringPosition += 1
                }
            }
        }
        return false
    }

    fun match(string: String) = match(string.toList())
}