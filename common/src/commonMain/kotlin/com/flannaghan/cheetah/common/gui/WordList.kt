package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
fun WordList(words: List<Word>, selectedWord: Word? = null, onClick: (Word) -> Unit = { }) {
    LazyColumn {
        items(words) { word ->
            WordListItem(
                word,
                onClick = {
                    onClick(word)
                },
                selected = selectedWord == word
            )
        }
    }
}

@Composable
fun WordListItem(word: Word, onClick: () -> Unit, selected: Boolean) {
    val backgroundColour = if (selected) MaterialTheme.colors.secondary else MaterialTheme.colors.background
    Row(modifier = Modifier.clickable { onClick() }.background(color = backgroundColour)) {
        Text(
            word.string, style = MaterialTheme.typography.body1, overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
        Spacer(Modifier.weight(1f))
        Text(
            word.entry, style = MaterialTheme.typography.body2, overflow = TextOverflow.Clip, maxLines = 1
        )
    }
}