package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopApplicationContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class WordListTextFileDataSourceKtTest {
    @Test
    fun testUKACD() {
        val context = DesktopApplicationContext()
        val file = File(context.dataPath(), "ukacd.txt")
        val defaults = DataSourceDefaults(useWordList = true, useDefinitions = true)
        val result = wordListTextFileDataSource("ukacd", file, Color.Red, defaults)
        assertEquals("ukacd", result.name)
        assertEquals(Color.Red, result.color)
        assertNull(result.definitionSearcher)

        val words = runBlocking {
            result.wordListFetcher.getWords(context)
        }
        assertNotEquals(0, words.size)
    }
}