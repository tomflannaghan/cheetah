package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word
import java.io.File

fun sqliteWordDatabaseDataSource(name: String, dbFile: File, color: Color, defaults: DataSourceDefaults): DataSource {
    val path = dbFile.absolutePath
    val wordListFetcher = object : WordListFetcher {
        override suspend fun getWords(context: ApplicationContext): List<Word> {
            val db = context.getWordDatabase(path)
            return db.wordQueries.selectAll().executeAsList().map { Word(it.word, it.canonical_form) }
        }
    }
    val definitionSearcher = object : DefinitionSearcher {
        override suspend fun lookupDefinition(context: ApplicationContext, word: Word): String {
            val db = context.getWordDatabase(path)
            return db.definitionQueries
                .definitionForWord(word.entry)
                .executeAsList()
                .joinToString("\n") {
                    val header = if (it.relationship == "") {
                        it.word_definition
                    } else {
                        "${it.relationship} ${it.word_definition}"
                    }
                    "=$header=\n${it.definition}"
                }
        }
    }
    return DataSource(name, wordListFetcher, definitionSearcher, color, defaults)
}