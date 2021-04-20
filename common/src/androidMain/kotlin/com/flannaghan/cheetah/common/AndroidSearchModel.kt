package com.flannaghan.cheetah.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AndroidSearchModel(private val viewModel: SearchViewModel) : SearchModel() {
    @Composable
    override fun queryState(): State<String> = viewModel.query.observeAsState("")

    @Composable
    override fun resultState(): State<SearchResult> =
        viewModel.result.observeAsState(SearchResult(true, "", listOf()))

    override fun updateQuery(query: String) {
        viewModel.query.value = query
    }

    override fun updateResult(result: SearchResult) {
        viewModel.result.value = result
    }
}

class SearchViewModel : ViewModel() {
    val query = MutableLiveData("")
    val result = MutableLiveData(SearchResult(true, "", listOf()))
}