package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.DesktopApplicationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
    fun dataSources() {
        val context = DesktopApplicationContext()
        val sources = dataSources(context)
        assertNotEquals(0, sources.size)
    }
}