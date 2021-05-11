package com.flannaghan.cheetah.common.search

/**
 * Our custom pattern. Regex-like but cut down in some respects, and more fully featured in others.
 * - Anagrams
 * - Misprints
 */
data class CustomPattern(val components: List<Component>, val misprints: Int = 0) {
    constructor(vararg components: Component, misprints: Int = 0) : this(components.asList(), misprints)
}

sealed class Component

data class Letter(val letter: Char) : Component()
data class Anagram(val letterCounts: Map<Char, Int>, val numberOfDots: Int) : Component()
object Dot : Component()
data class SubWord(val backwards: Boolean = false) : Component()

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
                in 'A'..'Z' -> letterCounts[char] = (letterCounts[char] ?: 0) + 1
                else -> error("Unexpected character $char")
            }
        } else {
            when (char) {
                '/' -> inAnagram = true
                '.' -> components.add(Dot)
                '>' -> components.add(SubWord(false))
                '<' -> components.add(SubWord(true))
                in 'A'..'Z' -> components.add(Letter(char))
                else -> error("Unexpected character $char")
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
    var subWordState: SubWordState?,
    var misprints: Int,
)

private data class AnagramState(
    val remainingLetterCounts: MutableMap<Char, Int>,
    var numberOfDots: Int,
    var misprintsConsumed: Int,
)

private data class SubWordState(
    val node: PrefixSearchNode
)


/**
 * Generates a new state (or null = no new state available) given the results of a match.
 * [mutateExisting] defines whether the old state should be reused.
 */
private fun newState(state: State, matched: Boolean, complete: Boolean, mutateExisting: Boolean): State? {
    var newState: State = state
    var misprintConsumed = false
    if (!matched && state.misprints > 0) {
        // We consume a misprint if we failed to match and can do.
        misprintConsumed = true
        newState = state.copy(misprints = state.misprints - 1)

    }
    return when {
        !(matched || misprintConsumed) -> null
        complete -> {
            if (mutateExisting) {
                newState.patternPosition += 1
                newState.stringPosition += 1
                newState
            } else {
                newState.copy(
                    patternPosition = newState.patternPosition + 1,
                    stringPosition = newState.stringPosition + 1
                )
            }
        }
        else -> {
            if (mutateExisting) {
                newState.stringPosition += 1
                newState
            } else {
                newState.copy(stringPosition = newState.stringPosition + 1)
            }
        }
    }
}


private fun matchLetter(context: SearchContext, component: Component, c: Char, state: State): List<State> {
    return when (component) {
        is Letter -> {
            if (component.letter == c) {
                listOfNotNull(newState(state, matched = true, complete = true, mutateExisting = true))
            } else {
                listOfNotNull(newState(state, matched = false, complete = true, mutateExisting = true))
            }
        }
        is Anagram -> {
            if (state.anagramState == null) {
                state.anagramState = AnagramState(
                    component.letterCounts.toMutableMap(), component.numberOfDots, 0
                )
            }
            matchLetterAnagram(c, state, state.anagramState!!)
        }
        Dot -> listOfNotNull(newState(state, matched = true, complete = true, mutateExisting = true))
        is SubWord -> {
            val subWordState = state.subWordState ?: SubWordState(
                if (component.backwards) context.reversePrefixSearchTree else context.prefixSearchTree
            )
            if (state.subWordState == null) {
                state.subWordState = subWordState
            }
            // Does our letter match?
            val newStates = mutableListOf<State?>()
            if (subWordState.node.matches(c)) {
                val nextSubWordState = subWordState.copy(node = subWordState.node.descend(c))
                state.subWordState = nextSubWordState
                newStates.add(newState(state, matched = true, complete = false, mutateExisting = false))
                if (nextSubWordState.node.isWord) {
                    state.subWordState = null
                    newStates.add(newState(state, matched = true, complete = true, mutateExisting = false))
                }
            } else {
                for (nextC in subWordState.node.nextLetters) {
                    val newSubWordState = subWordState.copy(node = subWordState.node.descend(nextC))
                    state.subWordState = newSubWordState
                    newStates.add(newState(state, matched = false, complete = false, mutateExisting = false))
                    if (newSubWordState.node.isWord) {
                        state.subWordState = null
                        newStates.add(newState(state, matched = false, complete = true, mutateExisting = false))
                    }
                }
            }
            newStates.filterNotNull()
        }
    }
}

/**
 * Matches against c, modifying anagram state as appropriate so that if we decide to continue the match, no
 * other state changes are required.
 */
private fun matchLetterAnagram(c: Char, state: State, anagramState: AnagramState): List<State> {
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
        !match && done -> listOfNotNull(newState(state, matched = false, complete = true, mutateExisting = true))
        !match && !done -> listOfNotNull(newState(state, matched = false, complete = false, mutateExisting = true))
        match && done -> listOfNotNull(newState(state, matched = true, complete = true, mutateExisting = true))
        else -> listOfNotNull(newState(state, matched = true, complete = false, mutateExisting = true))
    }
}


class CustomPatternEvaluator(private val pattern: CustomPattern) {
    private fun match(context: SearchContext, chars: List<Char>): Boolean {
        val states = ArrayDeque<State>()
        states.addLast(State(0, 0, null, null, pattern.misprints))
        while (states.size > 0) {
            val current = states.removeLast()
            // If we simultaneously reach the end of both, and we've used up all of the misprints, we're done.
            // But if we reach either end without the other, return false.
            if (current.patternPosition == pattern.components.size) {
                // If we simultaneously reach the end of both, and we've used up all of the misprints, we're done.
                if (current.stringPosition == chars.size && current.misprints == 0) return true
            } else if (current.stringPosition != chars.size) {
                // Otherwise, if we still have chars to consume, we try to consume the next char and pattern.
                val char = chars[current.stringPosition]
                val component = pattern.components[current.patternPosition]
                states.addAll(matchLetter(context, component, char, current))
            }
            // Otherwise we'll try the next state in the stack of states.
        }
        return false
    }

    fun match(context: SearchContext, string: String) = match(context, string.toList())
}