package com.flannaghan.cheetah.common.db

import app.cash.sqldelight.db.SqlDriver


expect class DatabaseDriverFactory {
    fun createDriver(filename: String): SqlDriver
}