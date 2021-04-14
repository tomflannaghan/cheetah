package com.flannaghan.cheetah.common.words

import java.io.InputStreamReader

interface WordSource {
    val name: String
    val words: List<Word>
}


class FileWordSource(override val name: String, reader: InputStreamReader) : WordSource {
    override val words = reader.readLines().map { Word(it.trim()) }
}
