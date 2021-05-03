package com.flannaghan.cheetah.common.db

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(val context: Context) {
    actual fun createDriver(filename: String): SqlDriver {
        return AndroidSqliteDriver(WordDatabase.Schema, context, filename)
    }
}