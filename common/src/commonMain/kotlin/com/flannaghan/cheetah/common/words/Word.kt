package com.flannaghan.cheetah.common.words

import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 */
data class Word(val string: String, val entry: String) {
    constructor(string: String) : this(string, stringToEntry(string))
}

private val LETTER_REGEX = Regex("[^\\p{L}\\n]")

fun stringToEntry(string: String): String {
    val entry = Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(LETTER_REGEX, "")
        .toUpperCase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
    return entry
}

/**
 * Takes a string with a word on each line, and converts them all to word instances. This is more efficient
 * than doing it one word at a time.
 */
fun stringToWords(string: String): List<Word> {
    val entries = Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(LETTER_REGEX, "")
        .toUpperCase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
        .split("\n")
    val words = string.split("\n")
    require(words.size == entries.size)
    return words.zip(entries).map { Word(it.first, it.second) }
}
