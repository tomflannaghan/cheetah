package com.flannaghan.cheetah.common.search.custompattern2

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ElementEvaluationTest {
    @Test
    fun anagram() {
        val e = AnagramEvaluation(mapOf('B' to 1, 'A' to 2, 'C' to 1), 1)
        assertTrue(e.matches("ACABX"))
        assertTrue(e.matches("XACAB"))
        assertTrue(e.matches("ACABA"))
        assertFalse(e.matches("ACYBX"))
        assertFalse(e.matches("ACXXA"))
        assertFalse(e.matches("ACXA"))
        assertFalse(e.matches("ACABXA"))
    }

    @Test
    fun letters() {
        val e = LetterSequenceEvaluation("ABC.E")
        assertTrue(e.matches("ABCDE"))
        assertFalse(e.matches("ABCDEF"))
        assertFalse(e.matches("ABDCE"))
        assertFalse(e.matches("XBCDE"))
        assertFalse(e.matches("ABCDX"))
        assertFalse(e.matches("ABCD"))
    }

    @Test
    fun contains() {
        val e = ContainsEvaluation(
            LetterSequenceEvaluation("CAT"),
            LetterSequenceEvaluation("DOG"),
            false
        )
        assertTrue(e.matches("CDOGAT"))
        assertTrue(e.matches("CADOGT"))
        assertFalse(e.matches("CATDOG"))
        assertFalse(e.matches("DOGCAT"))
        assertFalse(e.matches("CADOXT"))
        assertFalse(e.matches("CDDOGT"))
    }

    @Test
    fun misprint() {
        val e = MisprintEvaluation(
            LetterSequenceEvaluation("CAT")
        )
        assertTrue(e.matches("CAB"))
        assertTrue(e.matches("BAT"))
        assertFalse(e.matches("CAT"))
        assertFalse(e.matches("DOG"))
        assertFalse(e.matches("CATX"))
    }

    @Test
    fun extraLetterInPattern() {
        val e = ExtraLetterInPatternEvaluation(
            LetterSequenceEvaluation("HELLO"),
            true
        )
        assertTrue(e.matches("HELO"))
        assertTrue(e.matches("ELLO"))
        assertTrue(e.matches("HELL"))
        assertFalse(e.matches("HELLO"))
        assertFalse(e.matches("HEL"))
        assertFalse(e.matches("HEXO"))
    }

    @Test
    fun extraLetterInString() {
        val e = ExtraLetterInStringEvaluation(
            LetterSequenceEvaluation("HELLO")
        )
        assertTrue(e.matches("HELLOW"))
        assertTrue(e.matches("HELLOO"))
        assertTrue(e.matches("HELLWO"))
        assertTrue(e.matches("WHELLO"))
        assertFalse(e.matches("HELLO"))
        assertFalse(e.matches("HELLW"))
        assertFalse(e.matches("WELLO"))
    }

    @Test
    fun doubleMisprint() {
        val e = MisprintEvaluation(
            MisprintEvaluation(
                LetterSequenceEvaluation("CAT")
            )
        )
        assertTrue(e.matches("CEB"))
        assertTrue(e.matches("BAE"))
        assertTrue(e.matches("BET"))
        assertFalse(e.matches("CAT"))
        assertFalse(e.matches("CAX"))
        assertFalse(e.matches("BAT"))
        assertFalse(e.matches("DOG"))
        assertFalse(e.matches("CATX"))
    }

}