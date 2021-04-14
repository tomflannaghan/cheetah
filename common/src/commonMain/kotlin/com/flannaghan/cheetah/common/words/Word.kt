package com.flannaghan.cheetah.common.words

import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 */
data class Word(val string: String, val entry: String) {
    constructor(string: String) : this(string, stringToEntry(string))
}

private val LETTER_REGEX = Regex("[^\\p{L}]")
private val AZ_REGEX = Regex("^[A-Z]*$")

fun stringToEntry(string: String): String {
    val entry = Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(LETTER_REGEX, "")
        .toUpperCase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
    require(AZ_REGEX.matches(entry)) {
        "$string unsuccessfully normalised to $entry"
    }
    return entry
}