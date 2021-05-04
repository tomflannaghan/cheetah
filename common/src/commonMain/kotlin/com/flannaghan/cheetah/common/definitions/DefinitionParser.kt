package com.flannaghan.cheetah.common.definitions

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

@Suppress("RegExpRedundantEscape") // Required on Android.
val LABEL_REGEX = Regex("\\{\\{(.+?)\\}\\}")

@Suppress("RegExpRedundantEscape")
val LINK_REGEX = Regex("\\[\\[(.+?\\|)?+(.+?)\\]\\]")

val SUPERSCRIPT_REGEX = Regex("<sup>(.+?)</sup>")

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
        println("Unparsable line: $string")
        return null
    }

    private fun parseSpan(string: String): List<SpanElement> {
        val regexes = listOf(LINK_REGEX, LABEL_REGEX, SUPERSCRIPT_REGEX)
        val (regex, match) = regexes
            .mapNotNull { it.find(string)?.let { match -> Pair(it, match) } }
            .sortedBy { it.second.range.first }
            .firstOrNull() ?: return listOf(Text(string))


        val element = when (regex) {
            LINK_REGEX -> Link(match.groupValues.last())
            LABEL_REGEX -> Label(parseSpan(match.groupValues[1]))
            SUPERSCRIPT_REGEX -> Superscript(match.groupValues[1])
            else -> error("Shouldn't get here!")
        }

        val result = mutableListOf<SpanElement>()
        if (match.range.first != 0) {
            result.add(Text(string.substring(0, match.range.first)))
        }
        result.add(element)
        result.addAll(parseSpan(string.substring(match.range.last + 1, string.length)))
        return result
    }
}