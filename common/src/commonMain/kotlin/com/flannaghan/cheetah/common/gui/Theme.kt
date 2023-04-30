package com.flannaghan.cheetah.common.gui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp

class DefinitionTheme {
    companion object {
        val h1 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
        val h2 = TextStyle(fontSize = 14.sp, color = Color(181, 13, 2))
        val label = TextStyle(fontStyle = FontStyle.Italic)
        val superscript = TextStyle(fontSize = 12.sp, baselineShift = BaselineShift.Superscript)
        val body = TextStyle(fontSize = 14.sp)
        val number = TextStyle(fontSize = 14.sp)
        val etymology = TextStyle(color = Color.Gray, fontSize = 12.sp)
        val highlight = TextStyle(background = Color.Yellow)

        val link: TextStyle
            @Composable
            get() {
                return TextStyle(color = MaterialTheme.colors.primary)
            }
    }
}