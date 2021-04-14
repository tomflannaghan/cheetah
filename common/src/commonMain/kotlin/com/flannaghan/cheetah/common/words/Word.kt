package com.flannaghan.cheetah.common.words

import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 */
data class Word(val string: String, val entry: String) {
    constructor(string: String) : this(string, stringToEntry(string))
}

fun stringToEntry(string: String): String {
    val entry = Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(Regex("[^\\p{L}]"), "")
        .toUpperCase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
    require(Regex("^[A-Z]*$").matches(entry)) {
        "$string unsuccessfully normalised to $entry"
    }
    return entry
}