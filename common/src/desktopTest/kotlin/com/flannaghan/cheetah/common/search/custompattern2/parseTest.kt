package com.flannaghan.cheetah.common.search.custompattern2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParseTest {
    @Test
    fun toStringTree() {
        assertEquals(toStringTree("foo"), StringNode("foo", listOf()))
        assertEquals(
            toStringTree("i:(s:xyz),(b:(r:cat))"),
            StringNode(
                null, listOf(
                    StringNode("i:"), StringNode("s:xyz"),
                    StringNode(","),
                    StringNode(
                        null, listOf(
                            StringNode("b:"),
                            StringNode("r:cat")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun splitCommasBrackets() {
        assertEquals(splitCommasBrackets(""), listOf(listOf<String>()))
        assertEquals(splitCommasBrackets("hello"), listOf(listOf("hello")))
        assertEquals(splitCommasBrackets("hello,world"), listOf(listOf("hello"), listOf("world")))
        assertEquals(splitCommasBrackets("hello,(x,y)z,s"),
            listOf(listOf("hello"), listOf("(x,y)", "z"), listOf("s")))
    }

}