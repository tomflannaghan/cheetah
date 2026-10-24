package com.flannaghan.cheetah.common

import app.cash.sqldelight.db.SqlDriver
import java.io.File

/**
 * The platform-specific pieces of the app: where the data files live and how to open a database.
 */
interface Platform {
    val dataDir: File
    fun createSqlDriver(path: String): SqlDriver
}
