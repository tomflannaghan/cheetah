package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopPlatform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertNotEquals

internal class DataSourceKtTest {
    @Test
    fun parseColor() {
        assertEquals(Color.Blue, parseColor("#0000FF"))
        assertEquals(Color.Green, parseColor("#00FF00"))
        assertEquals(Color.Red, parseColor("#FF0000"))
        assertEquals(Color(1f / 16, 2f / 16, 3f / 16), parseColor("#102030"))
    }

    @Test
    fun skipsInvalidConfigs(@TempDir dir: File) {
        File(dir, "good.json").writeText("""{"name": "Good", "color": "#000000", "type": "TextFileWordList"}""")
        File(dir, "badJson.json").writeText("{not json")
        File(dir, "noColor.json").writeText("""{"name": "No colour", "type": "TextFileWordList"}""")
        File(dir, "badColor.json").writeText("""{"name": "Bad", "color": "blue", "type": "TextFileWordList"}""")
        for (name in listOf("good", "badJson", "noColor", "badColor")) File(dir, "$name.txt").writeText("a\tA\n")
        assertEquals(listOf("Good"), dataSources(DesktopPlatform(dir)).map { it.name })
    }

    @Test
    fun dataSources() {
        val sources = dataSources(DesktopPlatform())
        assertNotEquals(0, sources.size)
    }
}