package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.flannaghan.cheetah.common.SearchModel

@Composable
fun SearchableWordList(
    searchModel: SearchModel,
    selectedWordIndex: Int?,
    onQueryChanged: (String) -> Unit,
    onWordSelected: (Int?) -> Unit
) {
    // Manages focus between the text field and the word list.
    val wordListFocusRequester = remember { FocusRequester() }
    val textBoxFocusRequester = remember { FocusRequester() }
    val wordListDataSources = searchModel.wordListDataSources().value

    Column {
        val result = searchModel.resultState().value
        val queryText = searchModel.queryState().value

        ProgressTextField(
            queryText,
            onValueChange = {
                onQueryChanged(it)
            },
            keyboardOptions = KeyboardOptions(
                autoCorrect = false,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
            ),
            isBusy = queryText != result.query,
            isError = !result.success,
            modifier = Modifier
                .focusRequester(textBoxFocusRequester)
                .onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
                        wordListFocusRequester.requestFocus()
                        true
                    } else false
                }
        )
        Text("${result.words.size} results", style = MaterialTheme.typography.caption)
        WordList(
            result.words,
            selectedWordIndex,
            getDataSources = {
                searchModel.dataSourcesManager.getDataSources(it.bitmask).intersect(wordListDataSources).toList()
            },
            onClick = {
                wordListFocusRequester.requestFocus()
                onWordSelected(it)
            },
            modifier = Modifier
                .focusRequester(wordListFocusRequester).focusModifier()
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
                        textBoxFocusRequester.requestFocus()
                        true
                    } else false
                }
        )
    }
}

