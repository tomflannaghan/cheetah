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

}