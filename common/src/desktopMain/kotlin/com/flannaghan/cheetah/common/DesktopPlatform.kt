package com.flannaghan.cheetah.common

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

/**
 * [dataDir] defaults to the `cheetah.dataDir` system property, which the Gradle build sets for `run` and the tests.
 */
class DesktopPlatform(
    override val dataDir: File = File(
        System.getProperty("cheetah.dataDir") ?: error("The cheetah.dataDir system property is not set")
    ),
) : Platform {
    override fun createSqlDriver(path: String): SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$path")
}
