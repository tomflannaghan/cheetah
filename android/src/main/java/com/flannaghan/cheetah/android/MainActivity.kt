package com.flannaghan.cheetah.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.LocalContext
import com.flannaghan.cheetah.common.AndroidApplicationContext
import com.flannaghan.cheetah.common.AndroidSearchModel
import com.flannaghan.cheetah.common.SearchViewModel
import com.flannaghan.cheetah.common.gui.App
import com.flannaghan.cheetah.common.wordSources

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: SearchViewModel by viewModels()
        setContent {
            val searchModel = AndroidSearchModel(viewModel)
            val context = AndroidApplicationContext(LocalContext.current)
            searchModel.wordSources = wordSources(context)
            App(searchModel)
        }
    }
}