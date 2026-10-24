package com.flannaghan.cheetah.common

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.flannaghan.cheetah.common.db.WordDatabase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
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
    fun tearDown() = runBlocking {
        // Wait for the view model's background work to stop before the main dispatcher and the files go.
        for (viewModel in viewModels) viewModel.viewModelScope.coroutineContext.job.cancelAndJoin()
        Dispatchers.resetMain()
    }

    private val viewModels = mutableListOf<SearchViewModel>()

    private fun createViewModel() = SearchViewModel(DesktopPlatform(dir)).also { viewModels.add(it) }

    @Test
    fun search() = runTest {
        val viewModel = createViewModel()
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
    fun toggleDefinitions() = runTest {
        writeDefinitionDatabase("c", "C", "To purr")
        writeDefinitionDatabase("d", "D", "A feline", """, "useDefinitionsByDefault": false""")
        writeDefinitionDatabase("e", "E", null)
        val viewModel = createViewModel()
        assertEquals(listOf("C", "D"), viewModel.definitionDataSources.map { it.name })
        val (c, d) = viewModel.definitionDataSources
        assertEquals(setOf(c), viewModel.shownDefinitionDataSources.value)

        viewModel.updateQuery("cat")
        assertEquals("=cat=\nTo purr", viewModel.definition.first { it.isNotEmpty() })

        viewModel.toggleDefinitionDataSource(d)
        assertEquals("=cat=\nTo purr\n=cat=\nA feline", viewModel.definition.first { "feline" in it })

        viewModel.toggleDefinitionDataSource(c)
        assertEquals("=cat=\nA feline", viewModel.definition.first { "purr" !in it })
    }

    @Test
    fun fullTextSearchUsesShownDefinitions() = runTest {
        writeDefinitionDatabase("c", "C", "To purr")
        writeDefinitionDatabase("d", "D", "A feline", """, "useDefinitionsByDefault": false""")
        val viewModel = createViewModel()
        val (_, d) = viewModel.definitionDataSources

        viewModel.updateQuery("s:feline")
        assertEquals(listOf<String>(), viewModel.result.first { it.query == "s:feline" }.words)

        viewModel.toggleDefinitionDataSource(d)
        assertEquals(listOf("cat"), viewModel.result.first { it.words.isNotEmpty() }.words.map { it.string })
    }

    /**
     * Writes a SQLite source [name] with just the word "cat", defined as [definition] unless that is null.
     */
    private fun writeDefinitionDatabase(
        name: String, displayName: String, definition: String?, extraConfig: String = ""
    ) {
        File(dir, "$name.json").writeText(
            """{"name": "$displayName", "color": "#000000", "type": "SqliteWordDatabase"$extraConfig}"""
        )
        JdbcSqliteDriver("jdbc:sqlite:${File(dir, "$name.sqlite")}").use { driver ->
            WordDatabase.Schema.create(driver)
            driver.execute(null, "INSERT INTO word VALUES (1, 'cat', 'CAT')", 0)
            if (definition != null) {
                driver.execute(
                    null, "INSERT INTO definition (rowid, word, text) VALUES (1, 'cat', '=cat=\n$definition')", 0
                )
                driver.execute(null, "INSERT INTO wordDefinition VALUES (1, 0, 1)", 0)
            }
        }
    }

    @Test
    fun blankQueryHasNoResults() = runTest {
        val viewModel = createViewModel()
        viewModel.updateQuery("ca")
        viewModel.result.first { it.query == "ca" }
        viewModel.updateQuery(" ")
        assertEquals(listOf<String>(), viewModel.result.first { it.query == " " }.words)
    }
}
