package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
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
        TextField(
            searchModel.queryState().value,
            onValueChange = { scope.launch { searchModel.doSearch(it) } },
            colors = colors
        )
        WordList(result.words)
    }
}
