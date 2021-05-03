package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DefinitionView(definition: String) {
    val scrollState = rememberScrollState()
    Text(definition, modifier = Modifier.verticalScroll(scrollState))
}