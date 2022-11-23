package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class SqliteWordDatabaseDataSourceKtTest {
    @Test
    fun wiktionaryTest() {
        val context = DesktopApplicationContext()
        val file = File(context.dataPath(), "wiktionary.sqlite")
        val defaults = DataSourceDefaults(useWordList = true, useDefinitions = true)
        val source = SqliteWordDatabaseDataSource("wiktionary", file, Color.Blue, defaults)
        assertEquals("wiktionary", source.name)
        assertEquals(Color.Blue, source.color)
        val testWord = Word("goat", "GOAT", listOf(source))
        runBlocking {
            val words = source.getWords(context)
            assertTrue(testWord in words)
            val definition = source.lookupDefinition(context, testWord)
            assertTrue(
                definition.startsWith("=goat=\n==Noun==\n"),
                "$definition doesn't start as expected"
            )
        }
    }
}