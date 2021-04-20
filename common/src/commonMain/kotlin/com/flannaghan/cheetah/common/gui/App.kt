package com.flannaghan.cheetah.common.gui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flannaghan.cheetah.common.SearchModel
import com.flannaghan.cheetah.common.getPlatformName

@Composable
fun App(searchModel: SearchModel) {
    var text by remember { mutableStateOf("Hello, World!") }
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                text = "Hello, ${getPlatformName()}"
            }) {
                Text(text)
            }
            SearchableWordList(searchModel)
        }
    }
}
