package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
            isError = !result.success
        )
        Text("${result.words.size} results", style = MaterialTheme.typography.caption)
        WordList(result.words, selectedWordIndex, onClick = {
            onWordSelected(it)
        })
    }
}

