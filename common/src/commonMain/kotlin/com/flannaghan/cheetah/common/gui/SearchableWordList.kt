package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.flannaghan.cheetah.common.Searcher
import com.flannaghan.cheetah.common.WordList
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.stringToEntry

@Composable
fun SearchableWordList(searcher: Searcher) {
    Column {
        val isSuccess = searcher.resultState.value?.success ?: true
        val colors = if (isSuccess)
            TextFieldDefaults.textFieldColors()
        else
            TextFieldDefaults.textFieldColors(MaterialTheme.colors.error)

        TextField(
            searcher.currentQuery.value ?: "",
            onValueChange = { searcher.startSearch(it) },
            colors = colors
        )
        WordList(searcher.resultState.value?.words ?: listOf())
    }
}
