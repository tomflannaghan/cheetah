package com.flannaghan.cheetah.common.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(val context: Context) {
    actual fun createDriver(filename: String): SqlDriver {
        return AndroidSqliteDriver(WordDatabase.Schema, context, filename)
    }
}