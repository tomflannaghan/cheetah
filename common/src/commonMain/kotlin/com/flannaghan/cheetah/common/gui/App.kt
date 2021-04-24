package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.SearchModel
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.launch

@Composable
fun App(searchModel: SearchModel) {
    var selectedWord by remember { mutableStateOf<Word?>(null) }
    val scope = rememberCoroutineScope()
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.weight(1.0f)) {
                SearchableWordList(searchModel, selectedWord, onWordSelected = { word ->
                    selectedWord = word
                    searchModel.updateDefinition("")
                    if (word != null) scope.launch { searchModel.lookupDefinition(word) }
                })
            }
            if (selectedWord != null) {
                Row(Modifier.weight(1.0f).padding(top = 10.dp)) {
                    Text(searchModel.definitionState().value)
                }
            }
        }
    }
}
