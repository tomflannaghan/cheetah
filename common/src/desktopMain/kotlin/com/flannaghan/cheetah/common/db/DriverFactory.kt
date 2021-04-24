package com.flannaghan.cheetah.common.db

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return JdbcSqliteDriver("jdbc:sqlite:/home/tom/src/cheetah/data/wiktionary.sqlite")
    }

    fun doStuff() {
        val driver = DatabaseDriverFactory().createDriver()
        val db = WordDatabase(driver)
        val words = db.wordQueries.selectAllWithLimit(1).executeAsList()
        println(words)
    }
}