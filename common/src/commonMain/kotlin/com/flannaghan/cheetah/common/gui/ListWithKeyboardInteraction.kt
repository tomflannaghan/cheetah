package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import kotlin.math.max

@Composable
fun <T> ListWithKeyboardInteraction(
    items: List<T>,
    selectedIndex: Int?,
    onChangeSelection: (Int) -> Unit,
    modifier: Modifier = Modifier,
    renderItem: @Composable (index: Int, item: T) -> Unit
) {
    val scrollState = rememberLazyListState()

    // Scroll, clip indices, etc.
    fun onChangeSelection_(index: Int) {
        val clippedIndex = when {
            index < 0 -> 0
            index >= items.size -> items.size - 1
            else -> index
        }
        onChangeSelection(clippedIndex)
    }

    // Make sure we stay visible.
    val startVisible = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
    val endVisible = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
    val numberVisible = scrollState.layoutInfo.visibleItemsInfo.size
    val targetIndex = when {
        selectedIndex == null -> null
        selectedIndex < startVisible -> selectedIndex
        selectedIndex >= endVisible -> max(selectedIndex - numberVisible + 2, 0)
        else -> null
    }
    if (targetIndex != null) LaunchedEffect(targetIndex) { scrollState.scrollToItem(targetIndex) }

    LazyColumn(
        modifier.onPreviewKeyEvent {
            when {
                it.type != KeyEventType.KeyDown -> {
                    false
                }
                it.key == Key.DirectionUp -> {
                    if (selectedIndex != null) onChangeSelection_(selectedIndex - 1)
                    true
                }
                it.key == Key.DirectionDown -> {
                    if (selectedIndex != null) onChangeSelection_(selectedIndex + 1)
                    true
                }
                it.key == Key.PageUp -> {
                    onChangeSelection_(0)
                    true
                }
                it.key == Key.PageDown -> {
                    onChangeSelection_(items.size - 1)
                    true
                }
                else -> {
                    false
                }
            }
        },
        scrollState
    ) {
        itemsIndexed(items) { index, item -> renderItem(index, item) }
    }
}