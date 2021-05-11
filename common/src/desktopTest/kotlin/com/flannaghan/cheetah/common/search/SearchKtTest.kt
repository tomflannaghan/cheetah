package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class SearchKtTest {

    private val sampleContext = SearchContext(
        listOf(
            Word("hello", "HELLO"),
            Word("a b-c", "ABC"),
            Word("agc", "AGC"),
        )
    )

    @Test
    fun simpleSearch() {
        runBlocking {
            val result = search("a.c", sampleContext)
            assertTrue(result.success)
            assertEquals(listOf("a b-c", "agc"), result.words.map { it.string })
        }
    }

    @Test
    fun failSearch() {
        runBlocking {
            val result = search("a.c[", sampleContext)
            assertFalse(result.success)
            assertEquals(listOf<String>(), result.words.map { it.string })
        }
    }

    @Test
    fun andSearch() {
        runBlocking {
            val result = search("a.c\n.b.", sampleContext)
            assertTrue(result.success)
            assertEquals(listOf("a b-c"), result.words.map { it.string })
        }
    }
}