package com.flannaghan.cheetah.common.words

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.text.Normalizer

internal class WordKtTest {
    @Test
    fun stringToEntry() {
        assertEquals("HELLO", stringToEntry("hello"))
        assertEquals("APRESSKI", stringToEntry("après-ski"))
        assertEquals("ARDECHE", stringToEntry("Ardèche"))

        println("Ø".toLowerCase())
        assertEquals("ABO", stringToEntry("a b Ø"))
    }
}