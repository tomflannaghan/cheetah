package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.flannaghan.cheetah.common.datasource.DataSource

@Composable
fun DataSourceIcon(
    dataSource: DataSource,
    greyscale: Boolean = false,
    size: TextUnit = 20.sp
) {
    val color = (if (greyscale) toGreyscale(dataSource.color) else dataSource.color).copy(alpha = 0.3f)
    with(LocalDensity.current) {
        val sizeDp = size.toDp()
        val textSizeDp = sizeDp.times(0.7f)
        val paddingSizeDp = sizeDp.times(0.1f)
        Box(modifier = Modifier.padding(paddingSizeDp)) {
            Box(
                modifier = Modifier.size(sizeDp).clip(RoundedCornerShape(percent = 20)).background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dataSource.name.substring(0, 1), color = MaterialTheme.colors.background,
                    textAlign = TextAlign.Center, fontSize = textSizeDp.toSp()
                )
            }
        }
    }
}

@Composable
fun toGreyscale(color: Color): Color {
    val lum = color.luminance()
    return Color(red = lum, green = lum, blue = lum, alpha = LocalContentAlpha.current)
}