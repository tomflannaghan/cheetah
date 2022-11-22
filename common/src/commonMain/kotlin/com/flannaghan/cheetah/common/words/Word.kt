package com.flannaghan.cheetah.common.words

import com.flannaghan.cheetah.common.datasource.DataSource
import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 * We also need to keep track of which data sources have a given word.
 */
data class Word(val string: String, val entry: String, val dataSources: List<DataSource>) {
    constructor(string: String, dataSources: List<DataSource>) : this(string, stringToEntry(string), dataSources)
    constructor(string: String, dataSource: DataSource) : this(string, stringToEntry(string), listOf(dataSource))
}


private val LETTER_REGEX = Regex("[^\\p{L}]")

fun stringToEntry(string: String): String {
    val entry = Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(LETTER_REGEX, "")
        .toUpperCase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
    return entry
}
