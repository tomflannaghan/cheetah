package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopPlatform
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class WordListTextFileDataSourceKtTest {
    @Test
    fun testUKACD() = runBlocking {
        val file = File(DesktopPlatform().dataDir, "ukacd.txt")
        val defaults = DataSourceDefaults(useWordList = true, useDefinitions = true)
        val result = WordListTextFileDataSource("ukacd", file, Color.Red, defaults)
        assertEquals("ukacd", result.name)
        assertEquals(Color.Red, result.color)

        assertNotEquals(0, result.getWords().size)
    }
}