package com.flannaghan.cheetah.common.db

import com.squareup.sqldelight.db.SqlDriver


expect class DatabaseDriverFactory {
    fun createDriver(filename: String): SqlDriver
}