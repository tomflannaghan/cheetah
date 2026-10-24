package com.flannaghan.cheetah.common.gui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.flannaghan.cheetah.common.definitions.DefinitionParser
import com.flannaghan.cheetah.common.definitions.highlightDefinition

@Composable
fun DefinitionView(definition: String, highlightStrings: List<String> = listOf()) {
    val parsedDefinition = remember(definition, highlightStrings) {
        highlightStrings.fold(DefinitionParser().parse(definition), ::highlightDefinition)
    }
    DefinitionLines(parsedDefinition)
}