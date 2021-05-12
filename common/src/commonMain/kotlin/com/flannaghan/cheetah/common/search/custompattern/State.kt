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