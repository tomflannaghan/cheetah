package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.db.executeAsListSuspend
import com.flannaghan.cheetah.common.words.Word
import java.io.File

class SqliteWordDatabaseDataSource(
    name: String, dbFile: File, color: Color, defaults: DataSourceDefaults
) : DefinitionDataSource(name, color, defaults) {
    private val path = dbFile.absolutePath

    override suspend fun getWords(context: ApplicationContext): List<Word> {
        val db = context.getWordDatabaseCached(path)
        return db.wordQueries.selectAll().executeAsList()
            .map { Word(it.word, it.canonical_form) }
            .filter { it.entry.isNotEmpty() }
    }

    override suspend fun lookupDefinition(context: ApplicationContext, word: Word): String {
        val db = context.getWordDatabaseCached(path)
        // We use entries rather than words here. Could easily switch if we wanted.
        val parentWordRows = db.derivedWordQueries.parentWordsForEntry(word.entry).executeAsList()
        val words = db.wordQueries.wordsForEntry(word.entry).executeAsList().map { it.word }

        // Construct a map from {parentWord: [relationshipName]}
        val wordToRelationships =
            parentWordRows.groupBy { row -> row.parentWord }.mapValues {
                it.value.map { row -> row.relationshipName }.distinct()
            }

        val resultLines = mutableListOf<String>()
        for (word_ in words) {
            for (definition in db.definitionQueries.definitionsForWord(word_).executeAsList()) {
                resultLines.add(definition.text)
            }
        }

        for ((parentWord, relationships) in wordToRelationships) {
            for (parentDef in db.definitionQueries.definitionsForWord(parentWord).executeAsList()) {
                resultLines.add(parentDef.text)
            }
        }

        return resultLines.joinToString("\n")
    }

    override suspend fun fullTextSearch(
        context: ApplicationContext,
        allWords: List<Word>,
        pattern: String
    ): List<Word> {
        val db = context.getWordDatabaseCached(path)
        val canonicalForms = db.definitionQueries
            .fullTextSearch(pattern) { _, entry -> entry }
            .executeAsListSuspend()
            .toSet()
        val derivedCanonicalForms = db.definitionQueries
            .fullTextSearchDerivedWords(pattern) { _, entry -> entry }
            .executeAsListSuspend()
            .toSet()
        return allWords.filter { it.entry in canonicalForms || it.entry in derivedCanonicalForms }
    }

}