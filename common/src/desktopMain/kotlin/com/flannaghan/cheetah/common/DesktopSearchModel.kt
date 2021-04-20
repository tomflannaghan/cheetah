package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

class DesktopSearchModel : SearchModel() {
    private val _queryState = mutableStateOf("")
    private val _resultState = mutableStateOf(SearchResult(true, "", listOf()))

    @Composable
    override fun queryState(): State<String> = _queryState

    @Composable
    override fun resultState(): State<SearchResult> = _resultState

    override fun updateQuery(query: String) {
        _queryState.value = query
    }

    override fun updateResult(result: SearchResult) {
        _resultState.value = result
    }
}