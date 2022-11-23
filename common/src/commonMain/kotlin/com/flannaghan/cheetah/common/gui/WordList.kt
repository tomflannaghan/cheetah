package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.words.Word

@Composable
fun WordList(
    words: List<Word>,
    selectedWordIndex: Int? = null,
    getDataSources: (Word) -> List<DataSource>,
    onClick: (Int) -> Unit = { },
    modifier: Modifier = Modifier
) {
    ListWithKeyboardInteraction(
        words,
        selectedWordIndex,
        onChangeSelection = onClick,
        modifier = modifier
    ) { index, word ->
        WordListItem(
            word,
            getDataSources = getDataSources,
            onClick = {
                onClick(index)
            },
            selected = selectedWordIndex == index
        )
    }
}


@Composable
fun WordListItem(
    word: Word,
    onClick: () -> Unit,
    getDataSources: (Word) -> List<DataSource>,
    selected: Boolean
) {
    val backgroundColour = if (selected) MaterialTheme.colors.secondary else MaterialTheme.colors.background
    Row(modifier = Modifier.clickable(onClick = onClick).background(color = backgroundColour)) {
        Text(
            word.string, style = MaterialTheme.typography.body1, overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
        Spacer(Modifier.weight(1f))
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
            for (source in getDataSources(word)) {
                DataSourceIcon(source, greyscale = selected)
            }
        }
    }
}