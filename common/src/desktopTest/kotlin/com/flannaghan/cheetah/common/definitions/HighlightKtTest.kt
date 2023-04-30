package com.flannaghan.cheetah.common.definitions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HighlightKtTest {

    @Test
    fun simpleText() {
        assertEquals(
            Paragraph(listOf(Highlight(listOf(Text("hello!world"))))),
            highlightLineElement(Paragraph(listOf(Text("hello!world"))), "low")
        )

        assertEquals(
            Paragraph(listOf(Text("hello world"))),
            highlightLineElement(Paragraph(listOf(Text("hello world"))), "low")
        )
    }

    @Test
    fun multipleSpans() {
        println("Hello")
        val result = highlightLineElement(
            Paragraph(
                listOf(
                    Text("foo"),
                    Text("hello!"),
                    Text("world"),
                    Text("bar"),
                    Text("lowness"),
                )
            ), "low"
        )
        println(result)
        println("Hello again")

        assertEquals(
            Paragraph(
                listOf(
                    Text("foo"),
                    Highlight(
                        listOf(
                            Text("hello!"),
                            Text("world"),
                        )
                    ),
                    Text("bar"),
                    Highlight(listOf(Text("lowness")))
                )
            ),
            result
        )
    }

}
