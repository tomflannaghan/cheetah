package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.flannaghan.cheetah.common.search.fullTextSearchTerms
import kotlinx.coroutines.launch

@Composable
fun App(viewModel: SearchViewModel) {
    MaterialTheme {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        val wordListDataSources by viewModel.wordListDataSources.collectAsStateWithLifecycle()
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Cheetah 🐆") },
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
                    onToggleDataSource = viewModel::toggleWordListDataSource,
                )
            }
        ) { padding ->
            SearchPage(viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
fun Menu(
    dataSources: List<DataSource>,
    enabledDataSources: Set<DataSource>,
    onToggleDataSource: (DataSource) -> Unit,
) {
    Column {
        TopAppBar(title = { Text("Settings") }, backgroundColor = MaterialTheme.colors.secondary)
        Column(modifier = Modifier.padding(10.dp)) {
            Text("Word Lists", style = MaterialTheme.typography.h6)
            for (dataSource in dataSources) {
                DataSourceMenuRow(
                    dataSource,
                    enabled = dataSource in enabledDataSources,
                    onClick = { onToggleDataSource(dataSource) },
                )
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
