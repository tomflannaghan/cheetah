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

val HEADING_REGEX = Regex("(=+)(.+?)(=+)(.*)")
val ORDERED_LIST_REGEX = Regex("(#+)+(.*)")
val LINK_REGEX = Regex("(.+?\\|)?+(.+?)")  // Removes the target of links.

class DefinitionParser {
    private val orderedListStack = ArrayDeque<Int>()

    fun parse(string: String) = Definition(string.lines().flatMap { parseLine(it) })

    private fun parseLine(string: String): List<LineElement> {
        // Ignore empty lines.
        if (string.trim() == "") return listOf()

        val orderedListMatch = ORDERED_LIST_REGEX.matchEntire(string.trim())
        if (orderedListMatch != null) {
            val groups = orderedListMatch.groupValues
            val level = groups[1].length
            while (level < orderedListStack.size) orderedListStack.removeLast()
            while (level > orderedListStack.size) orderedListStack.addLast(0)
            orderedListStack[orderedListStack.size - 1]++
            return listOf(OrderedListItem(orderedListStack.last(), parseSpan(groups[2]), level))
        } else {
            orderedListStack.clear()
        }
        val headingMatch = HEADING_REGEX.matchEntire(string.trim())
        if (headingMatch != null) {
            val groups = headingMatch.groupValues
            return listOf(Heading(parseSpan(groups[2]), groups[1].length, parseSpan(groups[4])))
        }
        return listOf(Paragraph(parseSpan(string.trim())))
    }

    private data class Bracket(val start: String, val end: String, val makeElement: (String) -> SpanElement)

    private val brackets = listOf(
        Bracket("<~", "~>") { PartOfSpeech(parseSpan(it)) },
        Bracket("{{", "}}") { Label(parseSpan(it)) },
        Bracket("[[", "]]") {
            var linkContents = LINK_REGEX.matchEntire(it)?.groupValues?.last() ?: it
            // Links can't contain elements themselves so remove all brackets.
            for (seperator in listOf("{{", "}}", "[[", "]]")) {
                linkContents = linkContents.replace(seperator, "")
            }
            Link(linkContents)
        },
        Bracket("<sup>", "</sup>") { Superscript(it) },
        Bracket("<etym>", "</etym>") { Etymology(parseSpan(it)) },
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