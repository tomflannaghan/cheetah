package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.flannaghan.cheetah.common.SearchModel
import kotlinx.coroutines.launch

@Composable
fun SearchableWordList(searchModel: SearchModel) {
    Column {
        val result = searchModel.resultState().value
        val isSuccess = result.success
        val colors = if (isSuccess)
            TextFieldDefaults.textFieldColors()
        else
            TextFieldDefaults.textFieldColors(MaterialTheme.colors.error)

        val scope = rememberCoroutineScope()
        Row {
            TextField(
                searchModel.queryState().value,
                onValueChange = {
                    searchModel.updateQuery(it)
                    scope.launch { searchModel.doSearch(it) }
                },
                colors = colors,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Password,
                )
            )
        }
        Text("${result.words.size} results", style = MaterialTheme.typography.caption)
        WordList(result.words)
    }
}
