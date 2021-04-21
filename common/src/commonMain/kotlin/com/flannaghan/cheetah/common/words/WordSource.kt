package com.flannaghan.cheetah.common.words

import java.io.InputStreamReader

interface WordSource {
    val name: String
    val words: List<Word>
}


class FileWordSource(override val name: String, private val reader: InputStreamReader) : WordSource {
    override val words: List<Word> by lazy {
        stringToWords(reader.buffered().readText())
    }
}
