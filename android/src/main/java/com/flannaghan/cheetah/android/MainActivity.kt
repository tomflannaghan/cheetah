package com.flannaghan.cheetah.android

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.flannaghan.cheetah.common.AndroidApplicationContext
import com.flannaghan.cheetah.common.AndroidSearchModel
import com.flannaghan.cheetah.common.SearchViewModel
import com.flannaghan.cheetah.common.gui.App

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: SearchViewModel by viewModels()
        setContent {
            val scope = rememberCoroutineScope()
            val context = AndroidApplicationContext(LocalContext.current)
            val searchModel = remember { AndroidSearchModel(context, viewModel, scope) }
            App(searchModel)
        }
    }
}