package com.flannaghan.cheetah.common.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SearchQueryKtTest {
    @Test
    fun testParsingRegex() {
        assertEquals(RegexSearchQuery("HELLO...WORLD"), stringToSearchQuery("hello...World"))
    }

    @Test
    fun testParsingFTS() {
        assertEquals(FullTextSearchQuery("\"foo\""), stringToSearchQuery("s:foo"))
    }

    @Test
    fun testMultiline() {
        assertEquals(
            AndSearchQuery(
                RegexSearchQuery("A"),
                RegexSearchQuery("B"),
                FullTextSearchQuery("\"C\" AND \"D\"")
            ),
            stringToSearchQuery(
                """
                    A
                    s:C
                    B
                    s:D
                """.trimIndent()
            )
        )
    }

    @Test
    fun testToMatcher() {
        val query = AndSearchQuery(
            RegexSearchQuery("..A."),
            AndSearchQuery(
                listOf(FullTextSearchQuery(""), RegexSearchQuery("B"))
            )
        )
        assertEquals(
            AndMatcher(
                RegexMatcher("..A."),
                AndMatcher(FullTextSearchMatcher(""), RegexMatcher("B"))
            ),
            searchQueryToMatcher(query)
        )

    }
}