package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.flannaghan.cheetah.common.SearchModel
import kotlinx.coroutines.launch

@Composable
fun SearchableWordList(searchModel: SearchModel) {
    Column {
        val result = searchModel.resultState().value
        val scope = rememberCoroutineScope()
        val queryText = searchModel.queryState().value

        ProgressTextField(
            queryText,
            onValueChange = {
                searchModel.updateQuery(it)
                scope.launch { searchModel.doSearch(it) }
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
        WordList(result.words)
    }
}
