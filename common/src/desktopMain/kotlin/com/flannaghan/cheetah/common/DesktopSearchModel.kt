package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.flannaghan.cheetah.common.db.DatabaseDriverFactory
import com.flannaghan.cheetah.common.db.WordDatabase

class DesktopSearchModel : SearchModel() {
    private val _queryState = mutableStateOf("")
    private val _resultState = mutableStateOf(SearchResult(true, "", listOf()))
    private val _definitionState = mutableStateOf("")

    @Composable
    override fun queryState(): State<String> = _queryState

    @Composable
    override fun resultState(): State<SearchResult> = _resultState

    @Composable
    override fun definitionState(): State<String> = _definitionState

    override fun updateQuery(query: String) {
        _queryState.value = query
    }

    override fun updateResult(result: SearchResult) {
        _resultState.value = result
    }

    override fun updateDefinition(definition: String) {
        println("New def: $definition")
        _definitionState.value = definition
    }

    override fun getDatabase(): WordDatabase {
        return WordDatabase(DatabaseDriverFactory().createDriver())
    }
}