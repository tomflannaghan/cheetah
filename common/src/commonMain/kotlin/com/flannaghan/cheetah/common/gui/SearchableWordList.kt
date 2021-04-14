package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.flannaghan.cheetah.common.WordList
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.stringToEntry

@Composable
fun SearchableWordList(words: List<Word>) {
    val textState = remember { mutableStateOf(String()) }
    Column {
        TextField(textState.value, onValueChange = { textState.value = it })
        val prefix = stringToEntry(textState.value)
        WordList(words.filter { it.entry.startsWith(prefix) })
    }
}
