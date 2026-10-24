package com.flannaghan.cheetah.common.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(filename: String): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:$filename")
    }
}