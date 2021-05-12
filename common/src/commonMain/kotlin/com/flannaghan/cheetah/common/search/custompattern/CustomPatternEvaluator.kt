package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.SearchContext


private suspend fun matchLetter(context: SearchContext, component: Component, c: Char, state: State): List<State> {
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
                context.getPrefixSearchTree(component.backwards)
            )
            if (state.subWordState == null) {
                state.subWordState = subWordState
            }
            matchLetterSubWord(c, state, subWordState)
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


private fun matchLetterSubWord(c: Char, state: State, subWordState: SubWordState): List<State> {
    val newStates = mutableListOf<State?>()
    val matchingNode = subWordState.node.children.firstOrNull { it.char == c }
    if (matchingNode != null) {
        val nextSubWordState = subWordState.copy(node = matchingNode)
        state.subWordState = nextSubWordState
        newStates.add(newState(state, matched = true, complete = false, mutateExisting = false))
        if (nextSubWordState.node.isWord) {
            state.subWordState = null
            newStates.add(newState(state, matched = true, complete = true, mutateExisting = false))
        }
    } else {
        for (nonMatchingNode in subWordState.node.children) {
            if (nonMatchingNode.char == c) continue
            val newSubWordState = subWordState.copy(node = nonMatchingNode)
            state.subWordState = newSubWordState
            newStates.add(newState(state, matched = false, complete = false, mutateExisting = false))
            if (newSubWordState.node.isWord) {
                state.subWordState = null
                newStates.add(newState(state, matched = false, complete = true, mutateExisting = false))
            }
        }
    }
    return newStates.filterNotNull()
}


class CustomPatternEvaluator(private val pattern: CustomPattern) {
    private suspend fun match(context: SearchContext, chars: List<Char>): Boolean {
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

    suspend fun match(context: SearchContext, string: String) = match(context, string.toList())
}