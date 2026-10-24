package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopPlatform
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class SqliteWordDatabaseDataSourceKtTest {
    @Test
    fun wiktionaryTest() {
        val platform = DesktopPlatform()
        val file = File(platform.dataDir, "wiktionary.sqlite")
        val defaults = DataSourceDefaults(useWordList = true, useDefinitions = true)
        val source = SqliteWordDatabaseDataSource("wiktionary", file, Color.Blue, defaults, platform)
        assertEquals("wiktionary", source.name)
        assertEquals(Color.Blue, source.color)
        val testWord = Word("goat", "GOAT")
        runBlocking {
            val words = source.getWords()
            assertTrue(testWord in words)
            val definition = source.lookupDefinition(testWord)
            assertTrue(
                definition.startsWith("=goat=\n==Noun==\n"),
                "$definition doesn't start as expected"
            )
        }
    }
}