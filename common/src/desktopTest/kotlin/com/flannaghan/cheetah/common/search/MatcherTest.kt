package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MatcherTest {
    @Test
    fun regexMatch() {
        val matcher = RegexMatcher("A.C")
        runBlocking {
            assertEquals(
                listOf(true, false, true),
                matcher.match(SearchContext(listOf("AQC", "AOO", "ABC").map { Word("?", it) }))
            )
        }
    }

    @Test
    fun numWordsMatch() {
        val matcher = NumWordsMatcher(3)
        runBlocking {
            assertEquals(
                listOf(false, false, true, false),
                matcher.match(SearchContext(listOf(
                    Word("hello world", "HELLOWORLD"),
                    Word("foo", "FOO"),
                    Word("a b c", "ABC"),
                    Word("one two three four", "ONETWOTHREEFOUR"),
                )))
            )
        }
    }

    @Test
    fun parallelMatch() {
        val matcher = ParallelChunkMatcher(RegexMatcher(".."), 10)
        val entries = (1..10).flatMap { listOf("A", "B", "C", "AA", "BB", "CC") }.shuffled()
        runBlocking {
            assertEquals(
                entries.map { it.length == 2 },
                matcher.match(SearchContext(entries.map { Word("?", it) }))
            )
        }
    }

    @Test
    fun andMatch() {
        val matcher = AndMatcher(RegexMatcher("[1-5]"), RegexMatcher("[3-7]"))
        runBlocking {
            assertEquals(
                listOf(false, true, false, false),
                matcher.match(SearchContext(listOf("1", "4", "7", "XXX").map { Word("?", it) }))
            )
        }
    }

    @Test
    fun optimiseNoFTS() {
        assertEquals(
            ParallelChunkMatcher(
                AndMatcher(
                    RegexMatcher("XXX"),
                    RegexMatcher("X"),
                )
            ),
            optimize(
                AndMatcher(
                    RegexMatcher("X"),
                    RegexMatcher("X"),
                    RegexMatcher("XXX"),
                )
            )
        )
    }

    @Test
    fun optimiseFTS() {
        assertEquals(
            AndMatcher(
                FullTextSearchMatcher("hello"),
                ParallelChunkMatcher(RegexMatcher("XXX")),
            ),
            optimize(
                AndMatcher(
                    RegexMatcher("XXX"),
                    FullTextSearchMatcher("hello"),
                )
            )
        )
    }
}
