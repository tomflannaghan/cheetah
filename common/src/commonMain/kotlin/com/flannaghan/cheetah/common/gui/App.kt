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
import com.flannaghan.cheetah.common.SearchModel
import com.flannaghan.cheetah.common.datasource.DataSource
import kotlinx.coroutines.launch

@Composable
fun App(searchModel: SearchModel) {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Cheetah \uD83D\uDC06") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        drawerContent = {
            Menu(searchModel)
        }
    ) {
        SearchPage(searchModel)
    }
}


@Composable
fun Menu(searchModel: SearchModel) {
    val currentQuery = searchModel.queryState().value
    val coroutineScope = rememberCoroutineScope()
    Column {
        TopAppBar(title = { Text("Settings") }, backgroundColor = MaterialTheme.colors.secondary)
        Column(modifier = Modifier.padding(10.dp)) {
            Text("Word Lists", style = MaterialTheme.typography.h6)
            val enabledDataSources = searchModel.wordListDataSources().value
            for (dataSource in searchModel.dataSourcesManager.dataSources) {
                val enabled = dataSource in enabledDataSources
                val newDataSources = if (enabled) enabledDataSources.minus(dataSource)
                else enabledDataSources.plus(dataSource)
                DataSourceMenuRow(dataSource, enabled,
                    onClick = {
                        coroutineScope.launch {
                            searchModel.updateWordListDataSources(newDataSources)
                            searchModel.doSearch(currentQuery, newDataSources)
                        }
                    })
            }
        }
    }
}

@Composable
fun DataSourceMenuRow(dataSource: DataSource, enabled: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 5.dp)
            .clickable { onClick() }
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
fun SearchPage(searchModel: SearchModel) {
    var selectedWordIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val searchResult = searchModel.resultState().value
    val wordListDataSources = searchModel.wordListDataSources().value
    val highlightStrings = searchResult.query.lines().mapNotNull {
        Regex("s:(\\w+)\\*?").matchEntire(it)?.groupValues?.get(1)
    }

    LaunchedEffect(searchResult.words, selectedWordIndex) {
        val index = selectedWordIndex
        if (index == null || index >= searchResult.words.size) {
            searchModel.updateDefinition("")
        } else {
            val word = searchResult.words[index]
            searchModel.lookupDefinition(word)
        }
    }

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ResponsiveSplitLayout(300.dp, 16.dp,
                {
                    SearchableWordList(
                        searchModel,
                        selectedWordIndex,
                        onWordSelected = { index ->
                            selectedWordIndex = index
                        },
                        onQueryChanged = {
                            selectedWordIndex = null
                            searchModel.updateQuery(it)
                            scope.launch { searchModel.doSearch(it, wordListDataSources) }
                            selectedWordIndex = 0
                        }
                    )
                },
                {
                    DefinitionView(searchModel.definitionState().value, highlightStrings)
                }
            )
        }
    }
}
