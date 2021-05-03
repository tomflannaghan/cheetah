package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.definitions.*

@Composable
fun DefinitionLines(definition: Definition, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(definition.children) {
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
                    Row {
                        Spacer(Modifier.width(30.dp * (it.level - 1)))
                        Text("${it.number}.")
                        DefinitionSpan(it.contents)
                    }
                }
            }
        }
    }
}

@Composable
fun DefinitionSpan(elements: List<SpanElement>, modifier: Modifier = Modifier, style: TextStyle = TextStyle()) {
    Text(
        buildAnnotatedString {
            for (element in elements) {
                when (element) {
                    is Text -> append(element.text)
                    is Link -> {
                        withStyle(DefinitionTheme.link.toSpanStyle()) {
                            append(element.text)
                        }
                    }
                    is Label -> {
                        withStyle(DefinitionTheme.label.toSpanStyle()) {
                            append(element.text)
                        }
                    }
                }
            }
        },
        modifier = modifier,
        style = style
    )
}