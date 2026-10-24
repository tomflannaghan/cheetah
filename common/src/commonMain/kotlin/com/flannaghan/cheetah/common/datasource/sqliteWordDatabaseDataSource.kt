package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.Platform
import com.flannaghan.cheetah.common.db.WordDatabase
import com.flannaghan.cheetah.common.db.executeAsListSuspend
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.stringToEntry
import java.io.File

class SqliteWordDatabaseDataSource(
    name: String, dbFile: File, color: Color, defaults: DataSourceDefaults, platform: Platform
) : DefinitionDataSource(name, color, defaults) {
    private val db by lazy { WordDatabase(platform.createSqlDriver(dbFile.absolutePath)) }

    override suspend fun getWords(): List<Word> {
        return db.wordQueries.selectAll().executeAsList()
            .map { Word(it.word, it.canonical_form) }
            .filter { it.entry.isNotEmpty() }
    }

    override suspend fun lookupDefinition(word: Word): String {
        // We use entries rather than words here. Could easily switch if we wanted.
        val words = db.wordQueries.wordsForEntry(word.entry).executeAsList().map { it.word }
        val parentWords = db.derivedWordQueries.parentWordsForEntry(word.entry).executeAsList()
            .map { it.parentWord }
            .distinct()
        return (words + parentWords)
            .flatMap { db.definitionQueries.definitionsForWord(it).executeAsList() }
            .joinToString("\n") { it.text }
    }

    override suspend fun fullTextSearch(allWords: List<Word>, pattern: String): List<Word> {
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

    override suspend fun findLinkedWords(allWords: List<Word>, word: String): List<Word> {
        val canonicalForms = db.derivedWordQueries
            .childWordsForEntry(stringToEntry(word))
            .executeAsListSuspend()
            .map { it.canonical_form }
            .toSet()
        return allWords.filter { it.entry in canonicalForms }
    }
}
