package com.flannaghan.cheetah.common.search.custompattern


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
