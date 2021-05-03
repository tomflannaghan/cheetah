package com.flannaghan.cheetah.common

import android.content.Context

class AndroidApplicationContext(private val context: Context): ApplicationContext {
    override fun dataPath() = context.dataDir.absolutePath ?: error("Data dir not found!")
}