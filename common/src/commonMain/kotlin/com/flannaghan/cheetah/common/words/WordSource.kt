package com.flannaghan.cheetah.common.words

import java.io.InputStreamReader

interface WordSource {
    val name: String
    val words: List<String>
}


class FileWordSource(override val name: String, private val reader: InputStreamReader) : WordSource {
    override val words: List<String> by lazy {
        reader.buffered().readLines().map { it.trim() }
    }
}
