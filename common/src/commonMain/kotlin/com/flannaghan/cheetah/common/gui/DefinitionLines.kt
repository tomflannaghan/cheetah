package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.definitions.*

@Composable
fun DefinitionLines(definition: Definition, modifier: Modifier = Modifier) {
    val scrollState = rememberLazyListState()
    // Reset the scroll state to 0 whenever the definition changes.
    LaunchedEffect(definition) { scrollState.scrollToItem(0) }

    LazyColumn(modifier, scrollState) {
        items(definition.children) {
            Row(Modifier.fillMaxWidth()) {
                when (it) {
                    is Heading -> {
                        val style = when (it.level) {
                            1 -> DefinitionTheme.h1
                            2 -> DefinitionTheme.h2
                            else -> error("Too many levels in $it")
                        }
                        DefinitionSpan(it.contents, style = style, modifier = Modifier.padding(top = 8.dp))
                    }
                    is OrderedListItem -> {
                        Spacer(Modifier.width(30.dp * (it.level - 1)))
                        Text("${it.number}.", style = DefinitionTheme.number)
                        DefinitionSpan(it.contents, style = DefinitionTheme.body)
                    }
                }
            }
        }
    }
}

@Composable
fun DefinitionSpan(elements: List<SpanElement>, modifier: Modifier = Modifier, style: TextStyle = TextStyle()) {
    Text(
        elements.buildAnnotatedString(),
        modifier = modifier,
        style = style
    )
}

@Composable
fun List<SpanElement>.buildAnnotatedString(): AnnotatedString = buildAnnotatedString {
    for (element in this@buildAnnotatedString) {
        when (element) {
            is Text -> append(element.text)
            is Link -> {
                withStyle(DefinitionTheme.link.toSpanStyle()) {
                    append(element.text)
                }
            }
            is Label -> {
                withStyle(DefinitionTheme.label.toSpanStyle()) {
                    append(element.contents.buildAnnotatedString())
                }
            }
            is Superscript -> {
                withStyle(DefinitionTheme.superscript.toSpanStyle()) {
                    append(element.text)
                }
            }
        }
    }
}