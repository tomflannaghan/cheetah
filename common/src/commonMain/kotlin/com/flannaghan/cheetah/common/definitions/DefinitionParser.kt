package com.flannaghan.cheetah.common.definitions

import kotlin.text.Regex.Companion.escape

/**
 * Wiki syntax is taken from wikipedia.
 * Supports:
 * - only headers and ordered lists for structure (anything else will be skipped).
 * - links [[xxx|Xxx]] or [[...]].
 * - labels {{xxx}}.
 * - superscript <sup>..</sup>
 */

val HEADING_REGEX = Regex("(=+)(.+?)(=+)")
val ORDERED_LIST_REGEX = Regex("(#+)+(.*)")
val LINK_REGEX = Regex("(.+?\\|)?+(.+?)")  // Removes the target of links.

class DefinitionParser {
    private val orderedListStack = ArrayDeque<Int>()

    fun parse(string: String) = Definition(string.lines().mapNotNull { parseLine(it) })

    private fun parseLine(string: String): LineElement? {
        val orderedListMatch = ORDERED_LIST_REGEX.matchEntire(string)
        if (orderedListMatch != null) {
            val groups = orderedListMatch.groupValues
            val level = groups[1].length
            while (level < orderedListStack.size) orderedListStack.removeLast()
            while (level > orderedListStack.size) orderedListStack.addLast(0)
            orderedListStack[orderedListStack.size - 1]++
            return OrderedListItem(orderedListStack.last(), parseSpan(groups[2]), level)
        } else {
            orderedListStack.clear()
        }
        val headingMatch = HEADING_REGEX.matchEntire(string)
        if (headingMatch != null) {
            val groups = headingMatch.groupValues
            return Heading(parseSpan(groups[2]), groups[1].length)
        }
        return null
    }

    private data class Bracket(val start: String, val end: String, val makeElement: (String) -> SpanElement)

    private val brackets = listOf(
        Bracket("{{", "}}") { Label(parseSpan(it)) },
        Bracket("[[", "]]") {
            Link(LINK_REGEX.matchEntire(it)?.groupValues?.last() ?: it)
        },
        Bracket("<sup>", "</sup>") { Superscript(it) },
    )
    private val tokenRegex = Regex(brackets.joinToString("|") { "${escape(it.start)}|${escape(it.end)}" })
    private val bracketStarts = brackets.map { it.start }.toSet()

    private fun parseSpan(string: String): List<SpanElement> {
        var location = 0
        val stack = ArrayDeque<Pair<String, Int>>()
        val result = mutableListOf<SpanElement>()
        while (location < string.length) {
            val nextToken = tokenRegex.find(string, location) ?: break
            if (nextToken.value in bracketStarts) {
                if (nextToken.range.first != location && stack.size == 0) {
                    result.add(Text(string.substring(location until nextToken.range.first)))
                }
                stack.addLast(Pair(nextToken.value, nextToken.range.last + 1))
            } else if (stack.size != 0) {
                val (topStart, topLocation) = stack.last()
                val bracket = brackets.firstOrNull { it.start == topStart && it.end == nextToken.value }
                if (bracket != null) {
                    stack.removeLast()
                    if (stack.size == 0) {
                        result.add(
                            bracket.makeElement(string.substring(topLocation until nextToken.range.first))
                        )
                    }
                }
            }
            location = nextToken.range.last + 1
        }
        if (location != string.length) result.add(Text(string.substring(location)))
        return result
    }
}