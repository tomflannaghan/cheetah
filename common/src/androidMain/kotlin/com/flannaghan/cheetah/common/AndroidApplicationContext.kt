package com.flannaghan.cheetah.common

import android.content.Context
import java.io.InputStream

class AndroidApplicationContext(private val context: Context): ApplicationContext {
    override fun openFile(filename: String): InputStream {
        return context.assets.open(filename)
    }
}