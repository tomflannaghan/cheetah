package com.flannaghan.cheetah.common

import android.content.Context
import com.flannaghan.cheetah.common.db.DatabaseDriverFactory
import com.flannaghan.cheetah.common.db.WordDatabase

class AndroidApplicationContext(private val context: Context): ApplicationContext {
    override fun dataPath() = context.dataDir.absolutePath ?: error("Data dir not found!")
    override fun getWordDatabase(filePath: String): WordDatabase {
        return WordDatabase(DatabaseDriverFactory(context).createDriver(filePath))
    }
}