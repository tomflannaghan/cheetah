package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
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
                listOf(
                    RegexSearchQuery("A"),
                    RegexSearchQuery("B"),
                    FullTextSearchQuery("\"C\" AND \"D\"")
                )
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
            listOf(
                RegexSearchQuery("..A."),
                AndSearchQuery(
                    listOf(FullTextSearchQuery(""), RegexSearchQuery("B"))
                ),
            )
        )

        @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER")
        suspend fun fts(query: String, words: List<Word>) = words

        assertEquals(
            AndMatcher(
                listOf(
                    RegexMatcher("..A."),
                    AndMatcher(
                        listOf(FullTextSearchMatcher(::fts, ""), RegexMatcher("B"))
                    )

                )
            ),
            searchQueryToMatcher(query, ::fts)
        )

    }
}