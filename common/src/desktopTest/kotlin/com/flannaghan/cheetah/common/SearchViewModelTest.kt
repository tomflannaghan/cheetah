package com.flannaghan.cheetah.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
internal class SearchViewModelTest {
    @TempDir
    lateinit var dir: File

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        File(dir, "a.json").writeText("""{"name": "A", "color": "#000000", "type": "TextFileWordList"}""")
        File(dir, "a.txt").writeText("cat\tCAT\ncar\tCAR\ndog\tDOG\n")
        File(dir, "b.json").writeText(
            """{"name": "B", "color": "#000000", "type": "TextFileWordList", "useWordListByDefault": false}"""
        )
        File(dir, "b.txt").writeText("cab\tCAB\n")
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun search() = runTest {
        val viewModel = SearchViewModel(DesktopPlatform(dir))
        assertEquals(listOf("A", "B"), viewModel.dataSourcesManager.dataSources.map { it.name })

        viewModel.updateQuery("ca")
        assertEquals(listOf("car", "cat"), viewModel.result.first { it.query == "ca" }.words.map { it.string })
        assertEquals(0, viewModel.selectedWordIndex.value)

        viewModel.toggleWordListDataSource(viewModel.dataSourcesManager.dataSources[1])
        assertEquals(
            listOf("cab", "car", "cat"),
            viewModel.result.first { it.words.size == 3 }.words.map { it.string }
        )
    }

    @Test
    fun blankQueryHasNoResults() = runTest {
        val viewModel = SearchViewModel(DesktopPlatform(dir))
        viewModel.updateQuery("ca")
        viewModel.result.first { it.query == "ca" }
        viewModel.updateQuery(" ")
        assertEquals(listOf<String>(), viewModel.result.first { it.query == " " }.words)
    }
}
