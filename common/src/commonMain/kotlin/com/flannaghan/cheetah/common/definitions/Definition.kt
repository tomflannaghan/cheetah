package com.flannaghan.cheetah.common.definitions

data class Definition(val children: List<LineElement>)

/**
 * Any component of a definition.
 */
sealed class DefinitionPart

/**
 * Block elements that occupy whole lines.
 */
sealed class LineElement : DefinitionPart()
data class Heading(
    val contents: List<SpanElement>, val level: Int,
    val detail: List<SpanElement> = listOf()
) : LineElement()

data class OrderedListItem(val number: Int, val contents: List<SpanElement>, val level: Int) : LineElement()
data class Paragraph(val contents: List<SpanElement>) : LineElement()

/**
 * Span elements that form text.
 */
sealed class SpanElement : DefinitionPart()
sealed class LeafSpanElement : SpanElement() {
    abstract val text: String
}

sealed class ContentsSpanElement : SpanElement() {
    abstract val contents: List<SpanElement>
    abstract fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement
}

data class Text(override val text: String) : LeafSpanElement()
data class Link(override val text: String) : LeafSpanElement()
data class Superscript(override val text: String) : LeafSpanElement()
data class Label(override val contents: List<SpanElement>) : ContentsSpanElement() {
    override fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement = Label(newContents)
}

data class Etymology(override val contents: List<SpanElement>) : ContentsSpanElement() {
    override fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement = Etymology(newContents)
}

data class PartOfSpeech(override val contents: List<SpanElement>) : ContentsSpanElement() {
    override fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement = PartOfSpeech(newContents)
}

data class Headword(override val contents: List<SpanElement>) : ContentsSpanElement() {
    override fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement = Headword(newContents)
}

data class Highlight(override val contents: List<SpanElement>) : ContentsSpanElement() {
    override fun withNewContents(newContents: List<SpanElement>): ContentsSpanElement = Highlight(newContents)
}