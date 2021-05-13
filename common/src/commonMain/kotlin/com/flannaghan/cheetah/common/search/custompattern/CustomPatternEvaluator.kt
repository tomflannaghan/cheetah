package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.SearchContext


/**
 * Performs matches on a [CustomPattern]. This class is not threadsafe/concurrency-safe, so should be called
 * from within a single suspend function.
 */
class CustomPatternEvaluator(private val pattern: CustomPattern) {
    private val states = ArrayDeque<State>()

    private suspend fun match(context: SearchContext, chars: List<Char>): Boolean {
        // Clear initial state if anything is remaining.
        if (states.size > 0) states.clear()
        // Place the initial state in.
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
                matchLetter(context, component, char, current)
            }
            // Otherwise we'll try the next state in the stack of states.
        }
        return false
    }

    suspend fun match(context: SearchContext, string: String) = match(context, string.toList())

    /**
     * Adds a state to the list of new states. This adds [state] so [state] should not be used again once added.
     */
    private fun addState(state: State, matched: Boolean, complete: Boolean) {
        var misprintConsumed = false
        if (!matched && state.misprints > 0) {
            // We consume a misprint if we failed to match and can do.
            misprintConsumed = true
            state.misprints--
        }
        state.stringPosition++
        when {
            !(matched || misprintConsumed) -> {
                return
            }
            complete -> {
                state.patternPosition++
            }
        }
        states.add(state)
    }

    private fun addCopyState(state: State, matched: Boolean, complete: Boolean) {
        addState(state.copy(), matched, complete)
    }

    /**
     * Matches a single letter.
     */
    private suspend fun matchLetter(context: SearchContext, component: Component, c: Char, state: State) {
        when (component) {
            is Letter -> {
                if (component.letter == c) {
                    addState(state, matched = true, complete = true)
                } else {
                    addState(state, matched = false, complete = true)
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
            Dot -> addState(state, matched = true, complete = true)
            is SubWord -> {
                val subWordState = state.subWordState ?: SubWordState(
                    context.getPrefixSearchTree(component.backwards)
                )
                if (state.subWordState == null) {
                    state.subWordState = subWordState
                }
                matchLetterSubWord(c, state, subWordState)
            }
            is SubWordMatch -> {
                val subWordState = state.subWordState ?: SubWordState(
                    context.getPrefixSearchTreeForMatcher(component.matcher, component.backwards)
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
    private fun matchLetterAnagram(c: Char, state: State, anagramState: AnagramState) {
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
        when {
            !match && done -> addState(state, matched = false, complete = true)
            !match && !done -> addState(state, matched = false, complete = false)
            match && done -> addState(state, matched = true, complete = true)
            else -> addState(state, matched = true, complete = false)
        }
    }

    private fun matchLetterSubWord(c: Char, state: State, subWordState: SubWordState) {
        val matchingNode = subWordState.node.children.firstOrNull { it.char == c }
        if (matchingNode != null) {
            val nextSubWordState = subWordState.copy(node = matchingNode)
            if (nextSubWordState.node.isWord) {
                state.subWordState = null
                addCopyState(state, matched = true, complete = true)
            }
            state.subWordState = nextSubWordState
            addState(state, matched = true, complete = false)
        } else {
            for (nonMatchingNode in subWordState.node.children) {
                if (nonMatchingNode.char == c) continue
                val newSubWordState = subWordState.copy(node = nonMatchingNode)
                state.subWordState = newSubWordState
                addCopyState(state, matched = false, complete = false)
                if (newSubWordState.node.isWord) {
                    state.subWordState = null
                    addCopyState(state, matched = false, complete = true)
                }
            }
        }
    }

}