package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.words.Word
import java.io.File

fun wordListTextFileDataSource(name: String, file: File, color: Color) = DataSource(
    name,
    object : WordListFetcher {
        override suspend fun getWords(): List<Word> {
            return file
                .readLines(Charsets.UTF_8)
                .map { it.split('\t', limit = 2) }
                .filter { it.size == 2 }
                .map { Word(it[0], it[1]) }
        }
    },
    null,
    color
)