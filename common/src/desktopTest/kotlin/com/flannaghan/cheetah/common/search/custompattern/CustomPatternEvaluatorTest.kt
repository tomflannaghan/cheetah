package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.RegexMatcher
import com.flannaghan.cheetah.common.search.SearchContext
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CustomPatternEvaluatorTest {
    val context = SearchContext(listOf("BAT", "HAT", "HOG", "HA", "TAT").map { Word("?", it) })

    @Test
    fun letters() = runBlocking {
        val pattern = CustomPattern(Letter('H'), Letter('I'))
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match(context, "HI"))
        assertFalse(evaluator.match(context, "HG"))
        assertFalse(evaluator.match(context, "H"))
        assertFalse(evaluator.match(context, "HIT"))
    }

    @Test
    fun anagram() = runBlocking {
        val pattern = CustomPattern(Anagram(mapOf('A' to 1, 'S' to 2), 0))
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match(context, "SAS"))
        assertTrue(evaluator.match(context, "ASS"))
        assertFalse(evaluator.match(context, "SA"))
        assertFalse(evaluator.match(context, "SASS"))
        assertFalse(evaluator.match(context, "SAQ"))
    }

    @Test
    fun dottedAnagram() = runBlocking {
        val pattern = CustomPattern(Anagram(mapOf('A' to 1, 'S' to 2), 2))
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match(context, "SAS"))
        assertFalse(evaluator.match(context, "ASS"))
        assertFalse(evaluator.match(context, "SA"))
        assertTrue(evaluator.match(context, "SASSA"))
        assertTrue(evaluator.match(context, "SAQTS"))
        assertFalse(evaluator.match(context, "SAQTV"))
    }

    @Test
    fun dot() = runBlocking {
        val pattern = CustomPattern(Dot, Dot)
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match(context, "HI"))
        assertTrue(evaluator.match(context, "HG"))
        assertFalse(evaluator.match(context, "H"))
        assertFalse(evaluator.match(context, "HIT"))
    }

    @Test
    fun misprint() = runBlocking {
        val pattern = CustomPattern(Letter('H'), Letter('I'), Dot, misprints = 1)
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match(context, "HI"))
        assertFalse(evaluator.match(context, "HIT")) // No misprint - an actual match.
        assertTrue(evaluator.match(context, "JIT"))
        assertTrue(evaluator.match(context, "HAT"))
        assertFalse(evaluator.match(context, "SAT")) // Two misprints.
    }

    @Test
    fun misprintAnagrams() = runBlocking {
        val pattern = CustomPattern(
            Letter('H'),
            Anagram(mapOf('A' to 1, 'B' to 2), 1),
            Letter('T'),
            misprints = 2
        )
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match(context, "HABBQT")) // Exact match.
        assertFalse(evaluator.match(context, "JABBQT")) // 1 misprint.
        assertTrue(evaluator.match(context, "JABBQM")) // 2 misprints in the letters.
        assertTrue(evaluator.match(context, "JABMQT")) // 1 in letter, 1 in anag.
        assertTrue(evaluator.match(context, "HABMQZ")) // 1 in letter, 1 in anag.
        assertTrue(evaluator.match(context, "HAVMQT")) // 2 in anag.
    }

    @Test
    fun subWord() = runBlocking {
        val pattern = CustomPattern(SubWord(), Letter('T'))
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match(context, "HATQ"))
        assertTrue(evaluator.match(context, "HATT"))
        assertTrue(evaluator.match(context, "HOGT"))
        assertFalse(evaluator.match(context, "HOG"))
    }

    @Test
    fun subWords() = runBlocking {
        val pattern = CustomPattern(SubWord(), SubWord())
        val evaluator = CustomPatternEvaluator(pattern)
        assertTrue(evaluator.match(context, "HATTAT"))
        assertTrue(evaluator.match(context, "HATAT"))
        assertFalse(evaluator.match(context, "HATA"))
        assertFalse(evaluator.match(context, "HATTATT"))
    }

    @Test
    fun subWordBackwards() = runBlocking {
        val pattern = CustomPattern(SubWord(true), Letter('T'))
        val evaluator = CustomPatternEvaluator(pattern)
        assertFalse(evaluator.match(context, "TAHQ"))
        assertTrue(evaluator.match(context, "TAHT"))
        assertTrue(evaluator.match(context, "GOHT"))
        assertFalse(evaluator.match(context, "OHG"))
        assertTrue(evaluator.match(context, "TABT"))
    }

    @Test
    fun subWordMatch() = runBlocking {
        val matcher = RegexMatcher("H..")
        val pattern = CustomPattern(SubWordMatch(matcher, true), Letter('T'))
        val evaluator = CustomPatternEvaluator(pattern)
        context.withEvaluation {
            assertFalse(evaluator.match(context, "TAHQ"))
            assertTrue(evaluator.match(context, "TAHT"))
            assertTrue(evaluator.match(context, "GOHT"))
            assertFalse(evaluator.match(context, "OHG"))
            assertFalse(evaluator.match(context, "TABT"))
        }
    }
}