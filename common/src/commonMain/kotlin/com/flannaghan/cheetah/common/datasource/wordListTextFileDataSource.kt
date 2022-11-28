package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word
import java.io.File

class WordListTextFileDataSource(name: String, private val file: File, color: Color, defaults: DataSourceDefaults) :
    DataSource(name, color, defaults) {
    override suspend fun getWords(context: ApplicationContext): List<Word> {
        return file
            .readLines(Charsets.UTF_8)
            .map { it.split('\t', limit = 2) }
            .filter { it.size == 2 && it[1].isNotEmpty() }
            .map { Word(it[0], it[1]) }
    }
}