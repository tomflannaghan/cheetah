package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.search.SearchResult
import com.flannaghan.cheetah.common.words.Word

@Composable
fun SearchableWordList(
    query: String,
    result: SearchResult,
    selectedWordIndex: Int?,
    onQueryChanged: (String) -> Unit,
    onWordSelected: (Int) -> Unit,
    getDataSources: (Word) -> List<DataSource>,
    modifier: Modifier = Modifier,
) {
    // Manages focus between the text field and the word list.
    val wordListFocusRequester = remember { FocusRequester() }
    val textBoxFocusRequester = remember { FocusRequester() }

    Column(modifier) {
        ProgressTextField(
            query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .focusRequester(textBoxFocusRequester)
                .onPreviewKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
                        wordListFocusRequester.requestFocus()
                        true
                    } else false
                },
            isError = !result.success,
            isBusy = query != result.query,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                capitalization = KeyboardCapitalization.None,
                keyboardType = KeyboardType.Password,
            ),
        )
        Text("${result.words.size} results", style = MaterialTheme.typography.caption)
        WordList(
            result.words,
            selectedWordIndex,
            getDataSources = getDataSources,
            onClick = {
                wordListFocusRequester.requestFocus()
                onWordSelected(it)
            },
            modifier = Modifier
                .focusRequester(wordListFocusRequester).focusTarget()
                .onKeyEvent {
                    if (it.type == KeyEventType.KeyDown && it.key == Key.Tab) {
                        textBoxFocusRequester.requestFocus()
                        true
                    } else false
                }
        )
    }
}
