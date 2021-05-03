package com.flannaghan.cheetah.common.gui

import androidx.compose.runtime.Composable
import com.flannaghan.cheetah.common.definitions.DefinitionParser

@Composable
fun DefinitionView(definition: String) {
    val parsedDefinition = DefinitionParser().parse(definition)
    DefinitionLines(parsedDefinition)
}