package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.SearchModel

@Composable
fun App(searchModel: SearchModel) {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SearchableWordList(searchModel)
        }
    }
}
