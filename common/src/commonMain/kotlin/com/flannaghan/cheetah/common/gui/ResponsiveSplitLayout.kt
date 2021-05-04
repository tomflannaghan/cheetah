package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun ResponsiveSplitLayout(
    maxTopLeftWidth: Dp,
    gapPadding: Dp = 16.dp,
    topLeft: @Composable () -> Unit,
    bottomRight: @Composable () -> Unit
) {
    BoxWithConstraints {
        if (maxWidth > maxTopLeftWidth * 2) {
            Row {
                Column(Modifier.width(maxTopLeftWidth)) {
                    topLeft()
                }
                Column(Modifier.weight(1f).padding(start = gapPadding)) {
                    bottomRight()
                }
            }
        } else {
            Column {
                Row(Modifier.weight(1f)) {
                    topLeft()
                }
                Row(Modifier.weight(1f).padding(top = gapPadding)) {
                    bottomRight()
                }
            }
        }
    }
}