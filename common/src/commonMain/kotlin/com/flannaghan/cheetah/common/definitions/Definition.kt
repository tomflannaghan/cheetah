package com.flannaghan.cheetah.common.definitions

data class Definition(val children: List<LineElement>)

/**
 * Any component of a definition.
 */
sealed class DefinitionPart

/**
 * Block elements that occupy whole lines.
 */
abstract class LineElement : DefinitionPart()
data class Heading(val contents: List<SpanElement>, val level: Int) : LineElement()
data class OrderedListItem(val number: Int, val contents: List<SpanElement>, val level: Int) : LineElement()

/**
 * Span elements that form text.
 */
abstract class SpanElement : DefinitionPart()
data class Text(val text: String) : SpanElement()
data class Link(val text: String) : SpanElement()
data class Label(val contents: List<SpanElement>) : SpanElement()
data class Superscript(val text: String) : SpanElement()
