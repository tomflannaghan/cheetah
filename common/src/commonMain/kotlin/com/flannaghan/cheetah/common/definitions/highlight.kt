package com.flannaghan.cheetah.common.definitions

import com.flannaghan.cheetah.common.datasource.DataSourceDecodeError

fun highlightDefinition(definition: Definition, string: String): Definition {
    return definition.copy(children = definition.children.map{highlightLineElement(it, string)})
}


/**
 * Searches for a string and highlights it (potentially highlighting more things too).
 */
fun highlightLineElement(lineElement: LineElement, string: String): LineElement {
    val cleanedString = cleanString(string)
    return when (lineElement) {
        is Paragraph -> lineElement.copy(contents = highlightSpanElements(lineElement.contents, cleanedString))
        is OrderedListItem -> lineElement.copy(contents = highlightSpanElements(lineElement.contents, cleanedString))
        is Heading -> lineElement.copy(
            contents = highlightSpanElements(lineElement.contents, cleanedString),
            detail = highlightSpanElements(lineElement.detail, cleanedString)
        )

        else -> throw UnknownTypeHighlightError(lineElement.javaClass.typeName)
    }
}

private fun highlightSpanElements(spanElements: List<SpanElement>, string: String) =
    highlightSpanElementsImpl(spanElements, string).first


private fun highlightSpanElementsImpl(
    spanElements: List<SpanElement>,
    string: String
): Pair<List<SpanElement>, String> {
    var totalText = ""
    val textIndices = mutableListOf<Int>()
    val newSpanElements = mutableListOf<SpanElement>()
    for (spanElement in spanElements) {
        val (newSpanElement, text) = highlightSpanElementImpl(spanElement, string)
        textIndices.add(totalText.length)
        newSpanElements.add(newSpanElement)
        totalText += text
    }

    var cursor = 0
    var spanCursor = 0
    val highlightedSpanElements = mutableListOf<SpanElement>()
    while (true) {
        cursor = totalText.indexOf(string, startIndex = cursor)
        if (cursor == -1) break
        val spanIndexStart = textIndices.indexOfLast { it < cursor }
        val spanIndexEnd = textIndices.indexOfFirst { it >= cursor + string.length }.let {
            if (it == -1) newSpanElements.size else it
        }
        while (spanCursor < spanIndexStart) {
            highlightedSpanElements.add(newSpanElements[spanCursor])
            spanCursor++
        }
        highlightedSpanElements.add(Highlight(newSpanElements.subList(spanIndexStart, spanIndexEnd)))
        spanCursor = spanIndexEnd
        cursor++
    }
    while (spanCursor < newSpanElements.size) {
        highlightedSpanElements.add(newSpanElements[spanCursor])
        spanCursor++
    }

    return Pair(highlightedSpanElements, totalText.replace(string, ""))
}

private fun highlightSpanElementImpl(spanElement: SpanElement, string: String): Pair<SpanElement, String> {
    when (spanElement) {
        is LeafSpanElement -> {
            val text = cleanString(spanElement.text)
            return if (string in text) Pair(Highlight(listOf(spanElement)), text.replace(string, ""))
            else Pair(spanElement, text)
        }

        is ContentsSpanElement -> {
            val (newContents, text) = highlightSpanElementsImpl(spanElement.contents, string)
            val newSpanElement = spanElement.withNewContents(newContents)
            return if (string in text) Pair(Highlight(listOf(newSpanElement)), text.replace(string, ""))
            else Pair(newSpanElement, text)
        }

        else -> throw UnknownTypeHighlightError(spanElement.javaClass.typeName)
    }
}


class UnknownTypeHighlightError(type: String) : DataSourceDecodeError(type)


private fun cleanString(string: String): String = string.replace(Regex("[^a-zA-Z ]"), "").toUpperCase()