package com.flannaghan.cheetah.common.definitions

val HEADING_REGEX = Regex("(=+)([^=]+)(=+)")
val ORDERED_LIST_REGEX = Regex("(#+)+(.*)")

@Suppress("RegExpRedundantEscape") // Required on Android.
val LABEL_REGEX = Regex("\\{\\{([^}]+)\\}\\}")

@Suppress("RegExpRedundantEscape")
val LINK_REGEX = Regex("\\[\\[([^]]+)\\]\\]")

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
        val linkMatch = LINK_REGEX.find(string)
        val labelMatch = LABEL_REGEX.find(string)
        val match = if (linkMatch != null && labelMatch != null) {
            if (linkMatch.range.first < labelMatch.range.first) linkMatch else labelMatch
        } else {
            linkMatch ?: labelMatch
        }
        return if (match == null) {
            listOf(Text(string))
        } else {
            val newElement = if (match == linkMatch) Link(match.groupValues[1]) else Label(match.groupValues[1])
            val result = mutableListOf<SpanElement>()
            if (match.range.first != 0) {
                result.add(Text(string.substring(0, match.range.first)))
            }
            result.add(newElement)
            result.addAll(parseSpan(string.substring(match.range.last + 1, string.length)))
            result
        }
    }
}