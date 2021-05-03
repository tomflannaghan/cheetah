package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class SqliteWordDatabaseDataSourceKtTest {
    @Test
    fun wiktionaryTest() {
        val context = DesktopApplicationContext()
        val file = File(context.dataPath(), "wiktionary.sqlite")
        val source = sqliteWordDatabaseDataSource("wiktionary", file, Color.Blue)
        assertEquals("wiktionary", source.name)
        assertEquals(Color.Blue, source.color)
        val testWord = Word("goat", "GOAT")
        runBlocking {
            val words = source.wordList.getWords(context)
            assertTrue(testWord in words)
            assertNotNull(source.definitionSearcher)
            val definition = source.definitionSearcher!!.lookupDefinition(context, testWord)
            assertTrue(
                definition.startsWith("==Noun=="),
                "$definition doesn't start as expected"
            )
        }
    }
}