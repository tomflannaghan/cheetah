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
}