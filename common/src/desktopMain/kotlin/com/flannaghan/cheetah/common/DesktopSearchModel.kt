package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.flannaghan.cheetah.common.datasource.DataSource
import com.flannaghan.cheetah.common.search.SearchResult
import kotlinx.coroutines.CoroutineScope

class DesktopSearchModel(context: ApplicationContext, scope: CoroutineScope) : SearchModel(context, scope) {
    private val _queryState = mutableStateOf("")
    private val _resultState = mutableStateOf(SearchResult(true, "", listOf()))
    private val _definitionState = mutableStateOf("")
    private val _wordListDataSources = mutableStateOf(setOf<DataSource>())

    @Composable
    override fun queryState(): State<String> = _queryState

    @Composable
    override fun resultState(): State<SearchResult> = _resultState

    @Composable
    override fun definitionState(): State<String> = _definitionState

    @Composable
    override fun wordListDataSources(): State<Set<DataSource>> = _wordListDataSources

    override fun updateQuery(query: String) {
        _queryState.value = query
    }

    override fun updateResult(result: SearchResult) {
        _resultState.value = result
    }

    override fun updateDefinition(definition: String) {
        _definitionState.value = definition
    }

    override fun updateWordListDataSources(dataSources: Set<DataSource>) {
        _wordListDataSources.value = dataSources
    }
}