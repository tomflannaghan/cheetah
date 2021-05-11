package com.flannaghan.cheetah.common.search

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CustomPatternEvaluatorTest {
    @Test
    fun letters() {
        val pattern = CustomPattern(Letter('H'), Letter('I'))
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match("HI"))
        assertFalse(evaluator.match("HG"))
        assertFalse(evaluator.match("H"))
        assertFalse(evaluator.match("HIT"))
    }

    @Test
    fun anagram() {
        val pattern = CustomPattern(Anagram(mapOf('A' to 1, 'S' to 2), 0))
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match("SAS"))
        assertTrue(evaluator.match("ASS"))
        assertFalse(evaluator.match("SA"))
        assertFalse(evaluator.match("SASS"))
        assertFalse(evaluator.match("SAQ"))
    }

    @Test
    fun dottedAnagram() {
        val pattern = CustomPattern(Anagram(mapOf('A' to 1, 'S' to 2), 2))
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match("SAS"))
        assertFalse(evaluator.match("ASS"))
        assertFalse(evaluator.match("SA"))
        assertTrue(evaluator.match("SASSA"))
        assertTrue(evaluator.match("SAQTS"))
        assertFalse(evaluator.match("SAQTV"))
    }

    @Test
    fun dot() {
        val pattern = CustomPattern(Dot, Dot)
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match("HI"))
        assertTrue(evaluator.match("HG"))
        assertFalse(evaluator.match("H"))
        assertFalse(evaluator.match("HIT"))
    }

    @Test
    fun misprint() {
        val pattern = CustomPattern(Letter('H'), Letter('I'), Dot, misprints = 1)
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match("HI"))
        assertFalse(evaluator.match("HIT")) // No misprint - an actual match.
        assertTrue(evaluator.match("JIT"))
        assertTrue(evaluator.match("HAT"))
        assertFalse(evaluator.match("SAT")) // Two misprints.
    }

    @Test
    fun misprintAnagrams() {
        val pattern = CustomPattern(
            Letter('H'),
            Anagram(mapOf('A' to 1, 'B' to 2), 1),
            Letter('T'),
            misprints = 2
        )
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match("HABBQT")) // Exact match.
        assertFalse(evaluator.match("JABBQT")) // 1 misprint.
        assertTrue(evaluator.match("JABBQM")) // 2 misprints in the letters.
        assertTrue(evaluator.match("JABMQT")) // 1 in letter, 1 in anag.
        assertTrue(evaluator.match("HABMQZ")) // 1 in letter, 1 in anag.
        assertTrue(evaluator.match("HAVMQT")) // 2 in anag.
    }
}