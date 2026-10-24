package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    fun scrollToMakeIndexVisible(index: Int) {
        // Make sure we stay visible.
        val startVisible = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        val endVisible = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val numberVisible = scrollState.layoutInfo.visibleItemsInfo.size
        val targetIndex = when {
            index < startVisible -> index
            index >= endVisible -> max(index - numberVisible + 2, 0)
            else -> null
        }
        if (targetIndex != null) scope.launch { scrollState.scrollToItem(targetIndex) }
    }

    // If items changes, scroll to the top.
    LaunchedEffect(items) { scrollToMakeIndexVisible(0) }

    // Scroll, clip indices, etc.
    fun onChangeSelectionViaKeyEvent(index: Int) {
        val clippedIndex = when {
            index < 0 -> 0
            index >= items.size -> items.size - 1
            else -> index
        }
        scrollToMakeIndexVisible(clippedIndex)
        onChangeSelection(clippedIndex)
    }

    LazyColumn(
        modifier.onPreviewKeyEvent {
            when {
                it.type != KeyEventType.KeyDown -> {
                    false
                }
                it.key == Key.DirectionUp -> {
                    if (selectedIndex != null) onChangeSelectionViaKeyEvent(selectedIndex - 1)
                    true
                }
                it.key == Key.DirectionDown -> {
                    if (selectedIndex != null) onChangeSelectionViaKeyEvent(selectedIndex + 1)
                    true
                }
                it.key == Key.PageUp -> {
                    onChangeSelectionViaKeyEvent(0)
                    true
                }
                it.key == Key.PageDown -> {
                    onChangeSelectionViaKeyEvent(items.size - 1)
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