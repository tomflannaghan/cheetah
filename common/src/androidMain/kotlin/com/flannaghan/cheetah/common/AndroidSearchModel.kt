package com.flannaghan.cheetah.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flannaghan.cheetah.common.db.DatabaseDriverFactory
import com.flannaghan.cheetah.common.db.WordDatabase

class AndroidSearchModel(private val context: Context, private val viewModel: SearchViewModel) : SearchModel() {
    @Composable
    override fun queryState(): State<String> = viewModel.query.observeAsState("")

    @Composable
    override fun resultState(): State<SearchResult> =
        viewModel.result.observeAsState(SearchResult(true, "", listOf()))

    @Composable
    override fun definitionState(): State<String> = viewModel.query.observeAsState("")

    override fun updateQuery(query: String) {
        viewModel.query.value = query
    }

    override fun updateResult(result: SearchResult) {
        viewModel.result.value = result
    }

    override fun updateDefinition(definition: String) {
        viewModel.definition.value = definition
    }

    override fun getDatabase(): WordDatabase {
        return WordDatabase(DatabaseDriverFactory(context).createDriver())
    }
}

class SearchViewModel : ViewModel() {
    val query = MutableLiveData("")
    val result = MutableLiveData(SearchResult(true, "", listOf()))
    val definition = MutableLiveData("")
}