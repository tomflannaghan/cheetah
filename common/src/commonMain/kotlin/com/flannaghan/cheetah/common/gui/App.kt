package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flannaghan.cheetah.common.SearchViewModel
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.datasource.DefinitionDataSource
import com.flannaghan.cheetah.common.search.fullTextSearchTerms
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: SearchViewModel) {
    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        val wordListDataSources by viewModel.wordListDataSources.collectAsStateWithLifecycle()
        val shownDefinitionDataSources by viewModel.shownDefinitionDataSources.collectAsStateWithLifecycle()
        Scaffold(
            scaffoldState = scaffoldState,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            topBar = {
                TopAppBar(
                    title = { Text("Cheetah 🐆") },
                    windowInsets = AppBarDefaults.topAppBarWindowInsets,
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Settings")
                        }
                    }
                )
            },
            drawerContent = {
                Menu(
                    viewModel.dataSourcesManager.dataSources,
                    wordListDataSources,
                    onToggleWordListDataSource = viewModel::toggleWordListDataSource,
                    viewModel.definitionDataSources,
                    shownDefinitionDataSources,
                    onToggleDefinitionDataSource = viewModel::toggleDefinitionDataSource,
                )
            }
        ) { padding ->
            SearchPage(viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun Menu(
    wordListDataSources: List<DataSource>,
    enabledWordListDataSources: Set<DataSource>,
    onToggleWordListDataSource: (DataSource) -> Unit,
    definitionDataSources: List<DefinitionDataSource>,
    enabledDefinitionDataSources: Set<DefinitionDataSource>,
    onToggleDefinitionDataSource: (DefinitionDataSource) -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text("Settings") },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            backgroundColor = MaterialTheme.colors.secondary,
        )
        Column(modifier = Modifier.padding(10.dp).verticalScroll(rememberScrollState())) {
            Text("Word Lists", style = MaterialTheme.typography.h6)
            for (dataSource in wordListDataSources) {
                DataSourceMenuRow(
                    dataSource,
                    enabled = dataSource in enabledWordListDataSources,
                    onClick = { onToggleWordListDataSource(dataSource) },
                )
            }
            if (definitionDataSources.isNotEmpty()) {
                Text("Definitions", style = MaterialTheme.typography.h6, modifier = Modifier.padding(top = 10.dp))
                for (dataSource in definitionDataSources) {
                    DataSourceMenuRow(
                        dataSource,
                        enabled = dataSource in enabledDefinitionDataSources,
                        onClick = { onToggleDefinitionDataSource(dataSource) },
                    )
                }
            }
        }
    }
}

@Composable
fun DataSourceMenuRow(dataSource: DataSource, enabled: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp).clickable(onClick = onClick)
    ) {
        val alphaLevel = if (enabled) ContentAlpha.high else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides alphaLevel) {
            DataSourceIcon(dataSource, greyscale = !enabled, size = 20.sp)
            Spacer(Modifier.width(10.dp))
            Text(dataSource.name, fontSize = 16.sp)
        }
    }
}

@Composable
fun SearchPage(viewModel: SearchViewModel, modifier: Modifier = Modifier) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()
    val selectedWordIndex by viewModel.selectedWordIndex.collectAsStateWithLifecycle()
    val definition by viewModel.definition.collectAsStateWithLifecycle()
    val wordListDataSources by viewModel.wordListDataSources.collectAsStateWithLifecycle()
    val highlightStrings = remember(result.query) { fullTextSearchTerms(result.query) }

    ResponsiveSplitLayout(
        maxTopLeftWidth = 300.dp,
        gapPadding = 16.dp,
        modifier = modifier.padding(16.dp),
        topLeft = {
            SearchableWordList(
                query,
                result,
                selectedWordIndex,
                onQueryChanged = viewModel::updateQuery,
                onWordSelected = viewModel::selectWord,
                getDataSources = {
                    viewModel.dataSourcesManager.getDataSources(it.bitmask).filter { ds -> ds in wordListDataSources }
                },
            )
        },
        bottomRight = {
            DefinitionView(definition, highlightStrings)
        }
    )
}
