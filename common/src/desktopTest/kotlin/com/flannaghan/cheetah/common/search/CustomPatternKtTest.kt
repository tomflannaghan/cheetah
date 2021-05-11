package com.flannaghan.cheetah.common.search

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CustomPatternKtTest {
    @Test
    fun parse() {
        assertEquals(
            CustomPattern(
                Anagram(mapOf('A' to 1, 'B' to 2), 2),
                Letter('F'), Dot, Dot, Letter('D'),
                Anagram(mapOf('A' to 1, 'D' to 1), 0),
            ),
            parseCustomPattern("/AB.B./F..D/AD")
        )
    }
}