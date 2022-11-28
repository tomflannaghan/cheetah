package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DataSourcesManagerTest {

    private val dummySource1 = DummyDataSource(listOf(Word("foo", "FOO"), Word("Hello", "HELLO")))
    private val dummySource2 = DummyDataSource(listOf(Word("Hello", "HELLO"), Word("xyz", "XYZ")))

    @Test
    fun getDataSourcesFromBitMask() {
        val manager = DataSourcesManager(listOf(dummySource1, dummySource2))
        assertEquals(listOf(dummySource1), manager.getDataSources(0b01))
        assertEquals(listOf(dummySource2), manager.getDataSources(0b10))
        assertEquals(listOf(dummySource1, dummySource2), manager.getDataSources(0b11))
        assertEquals(listOf<DataSource>(), manager.getDataSources(0b00))
    }

    @Test
    fun getAllWords() {
        val context = DesktopApplicationContext()
        val manager = DataSourcesManager(listOf(dummySource1, dummySource2))
        runBlocking {
            assertEquals(listOf<Word>(), manager.getAllWords(listOf(), context))
            assertEquals(
                dummySource1.words.map { Word(it.string, it.entry, 0b01) },
                manager.getAllWords(listOf(dummySource1), context)
            )
            assertEquals(
                listOf(
                    Word("Hello", "HELLO", 0b11),
                    Word("xyz", "XYZ", 0b10)
                ), manager.getAllWords(listOf(dummySource2), context)
            )
            assertEquals(
                listOf(
                    Word("foo", "FOO", 0b01),
                    Word("Hello", "HELLO", 0b11),
                    Word("xyz", "XYZ", 0b10)
                ),
                manager.getAllWords(listOf(dummySource1, dummySource2), context)
            )
        }
    }

    @Test
    fun getAllWords2() {
        // Same as above, but populates the data sources manager in a different order.
        val context = DesktopApplicationContext()
        val manager = DataSourcesManager(listOf(dummySource1, dummySource2))
        runBlocking {
            assertEquals(
                listOf(
                    Word("foo", "FOO", 0b01),
                    Word("Hello", "HELLO", 0b11),
                    Word("xyz", "XYZ", 0b10)
                ),
                manager.getAllWords(listOf(dummySource1, dummySource2), context)
            )
            assertEquals(
                listOf(
                    Word("Hello", "HELLO", 0b11),
                    Word("xyz", "XYZ", 0b10)
                ), manager.getAllWords(listOf(dummySource2), context)
            )
        }
    }


}


private class DummyDataSource(
    val words: List<Word>,
    name: String = "Dummy",
    color: Color = Color(1, 0, 0),
    defaults: DataSourceDefaults = DataSourceDefaults(true, true)
) :
    DataSource(name, color, defaults) {
    override suspend fun getWords(context: ApplicationContext) = words
}