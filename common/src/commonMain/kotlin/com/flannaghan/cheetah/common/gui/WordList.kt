package com.flannaghan.cheetah.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.flannaghan.cheetah.common.words.Word

@Composable
fun WordList(words: List<Word>) {
    LazyColumn {
        items(words) { word -> WordListItem(word) }
    }
}

@Composable
fun WordListItem(word: Word) {
    Row {
        Text(
            word.string, style = MaterialTheme.typography.body1, overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
        Spacer(Modifier.weight(1f))
        Text(
            word.entry, color = MaterialTheme.colors.secondary, style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Clip, maxLines = 1
        )
    }
}