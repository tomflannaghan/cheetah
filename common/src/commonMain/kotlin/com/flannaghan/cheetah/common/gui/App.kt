package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.SearchModel
import kotlinx.coroutines.launch

@Composable
fun App(searchModel: SearchModel) {
    var selectedWordIndex by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    val searchResult = searchModel.resultState().value

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
                            scope.launch { searchModel.doSearch(it) }
                            selectedWordIndex = 0
                        }
                    )
                },
                {
                    println("Foo!")
                    DefinitionView(searchModel.definitionState().value)
                }
            )
        }
    }
}
