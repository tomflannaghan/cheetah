package com.flannaghan.cheetah.common.gui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class DefinitionTheme {
    companion object {
        val h1 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
        val h2 = TextStyle(fontSize = 16.sp, lineHeight = 20.sp)
        val label = TextStyle(fontStyle = FontStyle.Italic)
        val link = TextStyle(color = Color.Blue)

    }
}