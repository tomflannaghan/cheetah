package com.flannaghan.cheetah.common.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(filename: String): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:$filename")
    }
}