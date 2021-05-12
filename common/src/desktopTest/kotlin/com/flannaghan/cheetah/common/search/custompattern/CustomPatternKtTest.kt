package com.flannaghan.cheetah.common.search.custompattern

import com.flannaghan.cheetah.common.search.CustomPatternMatcher
import com.flannaghan.cheetah.common.search.FullTextSearchMatcher
import com.flannaghan.cheetah.common.search.ParallelChunkMatcher
import com.flannaghan.cheetah.common.search.RegexMatcher
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
                misprints = 2
            ),
            parseCustomPattern("``/AB.B./F..D/AD")
        )
    }

    @Test
    fun parseSubWordMatch() {
        assertEquals(
            CustomPattern(
                SubWordMatch(
                    ParallelChunkMatcher(RegexMatcher("G.T")),
                    backwards = false
                ),
                Letter('E')
            ),
            parseCustomPattern(">(G.T)E")
        )
    }

    @Test
    fun parseSubWordMatchNested() {
        assertEquals(
            CustomPattern(
                SubWordMatch(
                    ParallelChunkMatcher(
                        CustomPatternMatcher(
                            CustomPattern(
                                Letter('G'),
                                SubWordMatch(
                                    FullTextSearchMatcher("\"FOO\""),
                                    backwards = true
                                )
                            )
                        )
                    ),
                    backwards = false
                ),
                Letter('E')
            ),
            parseCustomPattern(">(G<(S:FOO))E")
        )
    }
}