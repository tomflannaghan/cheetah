package com.flannaghan.cheetah.common.search.custompattern


internal data class State(
    var stringPosition: Int,
    var patternPosition: Int,
    var anagramState: AnagramState?,
    var subWordState: SubWordState?,
    var misprints: Int,
)

internal data class AnagramState(
    val remainingLetterCounts: MutableMap<Char, Int>,
    var numberOfDots: Int,
    var misprintsConsumed: Int,
)

internal data class SubWordState(
    val node: PrefixSearchNode
)


/**
 * Generates a new state (or null = no new state available) given the results of a match.
 * [mutateExisting] defines whether the old state should be reused.
 */
internal fun newState(state: State, matched: Boolean, complete: Boolean, mutateExisting: Boolean): State? {
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