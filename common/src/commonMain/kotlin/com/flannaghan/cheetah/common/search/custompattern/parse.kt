package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.optimize
import com.flannaghan.cheetah.common.search.searchQueryToMatcher
import com.flannaghan.cheetah.common.search.stringToSearchQuery


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
    var inSubWordQuery = false
    var subWordQueryBracketDepth = 0
    var previousCharSubWord = false
    var currentSubWordQuery = ""
    for (char in string) {
        when {
            inAnagram -> {
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
            }
            inSubWordQuery -> {
                currentSubWordQuery += char
                when (char) {
                    '(' -> {
                        subWordQueryBracketDepth++
                    }
                    ')' -> {
                        subWordQueryBracketDepth--
                        if (subWordQueryBracketDepth == -1) {
                            inSubWordQuery = false
                            val subWordComponent = components.removeLast()
                            val backwards = if (subWordComponent is SubWord) {
                                subWordComponent.backwards
                            } else {
                                error("Expected SubWord component, got $subWordComponent")
                            }
                            // Remove the final ) character before parsing.
                            val query = currentSubWordQuery.substring(0 until currentSubWordQuery.length - 1)
                            val matcher = optimize(searchQueryToMatcher(stringToSearchQuery(query)))
                            components.add(SubWordMatch(matcher, backwards))
                            currentSubWordQuery = ""
                        }
                    }
                }
            }
            else -> {
                when (char) {
                    '/' -> inAnagram = true
                    '.' -> components.add(Dot)
                    '>' -> components.add(SubWord(false))
                    '<' -> components.add(SubWord(true))
                    '(' -> if (previousCharSubWord) inSubWordQuery = true else error("Unexpected (")
                    in 'A'..'Z' -> components.add(Letter(char))
                    else -> error("Unexpected character $char")
                }
                previousCharSubWord = char in "<>"
            }
        }
    }
    if (inAnagram) {
        components.add(Anagram(letterCounts, dotCount))
    }
    if (inSubWordQuery) error("Incomplete subword query in $string")
    return CustomPattern(components)
}
