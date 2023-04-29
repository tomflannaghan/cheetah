package com.flannaghan.cheetah.common.definitions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DefinitionParserTest {

    @Test
    fun heading() {
        val parser = DefinitionParser()
        assertEquals(
            Definition(listOf(Heading(listOf(Text("Hello")), 1))),
            parser.parse("=Hello=")
        )
        assertEquals(
            Definition(listOf(Heading(listOf(Text("Hello")), 4))),
            parser.parse("====Hello====")
        )
    }

    @Test
    fun orderedList() {
        val parser = DefinitionParser()
        assertEquals(
            Definition(
                listOf(
                    OrderedListItem(1, listOf(Text("Item 1")), 1),
                    OrderedListItem(2, listOf(Text("Item 2")), 1),
                    OrderedListItem(1, listOf(Text("Item 2.1")), 2),
                    OrderedListItem(2, listOf(Text("Item 2.2")), 2),
                    OrderedListItem(3, listOf(Text("Item 3")), 1),
                    OrderedListItem(1, listOf(Text("Item 3.1")), 2),

                    )
            ),
            parser.parse(
                """
                #Item 1
                #Item 2
                ##Item 2.1
                ##Item 2.2
                #Item 3
                ##Item 3.1
            """.trimIndent()
            )
        )
    }

    @Test
    fun listReset() {
        val parser = DefinitionParser()
        assertEquals(
            Definition(
                listOf(
                    OrderedListItem(1, listOf(Text("Item 1")), 1),
                    OrderedListItem(2, listOf(Text("Item 2")), 1),
                    Heading(listOf(Text("Hello")), 1),
                    OrderedListItem(1, listOf(Text("Item 1")), 1),
                )
            ),
            parser.parse(
                """
                #Item 1
                #Item 2
                =Hello=
                #Item 1
            """.trimIndent()
            )
        )
    }

    @Test
    fun span() {
        val parser = DefinitionParser()
        assertEquals(
            Definition(
                listOf(
                    OrderedListItem(
                        1, listOf(
                            Text("Hello "),
                            Link("I'm a link not a label"),
                            Text(" and some text "),
                            Label(listOf(Text("and a label "), Link("with link"))),
                            Text(" !")
                        ), 1
                    )
                )
            ),
            parser.parse("#Hello [[I'm a link {{not a label}}]] and some text {{and a label [[with link]]}} !")
        )
    }

    @Test
    fun spanHeading() {
        val parser = DefinitionParser()
        assertEquals(
            Definition(
                listOf(
                    Heading(
                        contents = listOf(Text(text = "Hello")), level = 1,
                        detail = listOf(Text(" "), PartOfSpeech(contents = listOf(Text("Bye"))))
                    )
                )
            ),
            parser.parse("=Hello= <~Bye~>")
        )
    }
}