package com.flannaghan.cheetah.android

import com.flannaghan.cheetah.common.App
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.flannaghan.cheetah.common.AndroidApplicationContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(AndroidApplicationContext(LocalContext.current))
        }
    }
}