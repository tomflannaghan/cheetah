package com.flannaghan.cheetah.common.gui

import androidx.compose.runtime.Composable
import com.flannaghan.cheetah.common.definitions.DefinitionParser
import com.flannaghan.cheetah.common.definitions.highlightDefinition

@Composable
fun DefinitionView(definition: String, highlightStrings: List<String> = listOf()) {
    var parsedDefinition = DefinitionParser().parse(definition)
    for (highlightString in highlightStrings) parsedDefinition = highlightDefinition(parsedDefinition, highlightString)
    DefinitionLines(parsedDefinition)
}