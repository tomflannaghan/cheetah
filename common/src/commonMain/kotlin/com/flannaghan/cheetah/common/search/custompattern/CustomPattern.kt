package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.Matcher

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
data class SubWordMatch(val matcher: Matcher, val backwards: Boolean = false) : Component()


