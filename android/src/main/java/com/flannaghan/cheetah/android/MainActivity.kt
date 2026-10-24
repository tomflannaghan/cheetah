package com.flannaghan.cheetah.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.flannaghan.cheetah.common.AndroidPlatform
import com.flannaghan.cheetah.common.SearchViewModel
import com.flannaghan.cheetah.common.gui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(viewModel { SearchViewModel(AndroidPlatform(applicationContext)) })
        }
    }
}
