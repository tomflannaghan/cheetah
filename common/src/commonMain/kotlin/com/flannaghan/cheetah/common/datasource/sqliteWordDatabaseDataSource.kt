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

            // We use entries rather than words here. Could easily switch if we wanted.
            val parentWordRows = db.derivedWordQueries.parentWordsForEntry(word.entry).executeAsList()
            val words = db.wordQueries.wordsForEntry(word.entry).executeAsList().map { it.word }

            // Construct a map from {parentWord: [relationshipName]}
            val wordToRelationships =
                parentWordRows.groupBy { row -> row.parentWord }.mapValues {
                    it.value.map { it.relationshipName }.toSet().toList()
                }

            val resultLines = mutableListOf<String>()
            for (word_ in words) {
                for (definition in db.definitionQueries.definitionsForWord(word_).executeAsList()) {
                    resultLines.add("=$word_=")
                    resultLines.add(definition.definition)
                }
            }

            for ((parentWord, relationships) in wordToRelationships) {
                for (parentDef in db.definitionQueries.definitionsForWord(parentWord).executeAsList()) {
                    resultLines.add("=$parentWord (${relationships.joinToString(", ")})=")
                    resultLines.add(parentDef.definition)
                }
            }

            return resultLines.joinToString("\n")
        }
    }
    return DataSource(name, wordListFetcher, definitionSearcher, color, defaults)
}