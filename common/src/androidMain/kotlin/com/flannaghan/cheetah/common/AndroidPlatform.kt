package com.flannaghan.cheetah.common

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.flannaghan.cheetah.common.db.WordDatabase
import java.io.File

class AndroidPlatform(context: Context) : Platform {
    // Hold the application context, not an Activity, as this outlives configuration changes.
    private val context = context.applicationContext

    override val dataDir: File = this.context.dataDir

    override fun createSqlDriver(path: String): SqlDriver = AndroidSqliteDriver(WordDatabase.Schema, context, path)
}
