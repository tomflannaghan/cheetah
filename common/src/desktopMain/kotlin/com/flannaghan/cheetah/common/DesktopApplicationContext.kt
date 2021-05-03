package com.flannaghan.cheetah.common

import com.flannaghan.cheetah.common.db.DatabaseDriverFactory
import com.flannaghan.cheetah.common.db.WordDatabase

class DesktopApplicationContext : ApplicationContext {
    override fun dataPath() = "/home/tom/src/cheetah/data/"

    override fun getWordDatabase(filePath: String): WordDatabase {
        return WordDatabase(DatabaseDriverFactory().createDriver())
    }
}