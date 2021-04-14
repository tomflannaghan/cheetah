package com.flannaghan.cheetah.common

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun WordList(words: List<String>) {
    LazyColumn {
        items(words) { word -> WordListItem(word) }
    }
}

@Composable
fun WordListItem(word: String) {
    Text(word)
}