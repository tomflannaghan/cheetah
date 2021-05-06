package com.flannaghan.cheetah.common.db

import com.flannaghan.cheetah.common.DesktopApplicationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.system.measureTimeMillis

class QueryTest {

    fun getDatabase(): WordDatabase {
        val context = DesktopApplicationContext()
        val file = File(context.dataPath(), "wiktionary.sqlite")
        return context.getWordDatabase(file.absolutePath)
    }

    @Test
    fun findWords() {
        // 107..54
        val db = getDatabase()
        val time = measureTimeMillis {
            val words = db.wordQueries.wordsForEntry("CAFE").executeAsList()
            assertEquals(listOf("café", "cafe", "CAFE"), words.map { it.word })
        }
        println(time)
    }

    @Test
    fun parentWords() {
        // 334..58
        val db = getDatabase()
        val time = measureTimeMillis {
            val rows = db.derivedWordQueries.parentWordsForEntry("CAFES").executeAsList()
            assertEquals(
                setOf("café", "cafe", "CAFE").map { Pair(it, "plural of") }.toSet(),
                rows.map { Pair(it.parentWord, it.relationshipName) }.toSet()
            )
        }
        println(time)
    }

    @Test
    fun definitionsForWord() {
        // 162..53
        val db = getDatabase()
        val time = measureTimeMillis {
            val rows = db.definitionQueries.definitionsForWord("cafe").executeAsList()
            assertEquals(1, rows.size)
        }
        println(time)
    }
}