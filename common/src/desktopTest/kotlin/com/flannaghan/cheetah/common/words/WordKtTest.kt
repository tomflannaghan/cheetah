package com.flannaghan.cheetah.common.words

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class WordKtTest {
    @Test
    fun stringToEntry() {
        assertEquals("HELLO", stringToEntry("hello"))
        assertEquals("APRESSKI", stringToEntry("après-ski"))
        assertEquals("ARDECHE", stringToEntry("Ardèche"))
        assertEquals("ABO", stringToEntry("a b Ø"))
    }

    @Test
    fun stringToWords() {
        val expected = listOf(
            Word("hello", "HELLO"),
            Word("après-ski", "APRESSKI"),
            Word("Ardèche", "ARDECHE"),
            Word("a b Ø", "ABO"),
        )
        assertEquals(expected, stringToWords(expected.joinToString("\n") { it.string }))
    }
}