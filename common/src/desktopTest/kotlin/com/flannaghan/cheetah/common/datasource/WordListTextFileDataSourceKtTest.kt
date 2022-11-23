package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopApplicationContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class WordListTextFileDataSourceKtTest {
    @Test
    fun testUKACD() {
        val context = DesktopApplicationContext()
        val file = File(context.dataPath(), "ukacd.txt")
        val defaults = DataSourceDefaults(useWordList = true, useDefinitions = true)
        val result = WordListTextFileDataSource("ukacd", file, Color.Red, defaults)
        assertEquals("ukacd", result.name)
        assertEquals(Color.Red, result.color)

        val words = runBlocking {
            result.getWords(context)
        }
        assertNotEquals(0, words.size)
    }
}