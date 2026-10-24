package com.flannaghan.cheetah.common.words

import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 * We also need to keep track of which data sources have a given word.
 */
data class Word(val string: String, val entry: String, val bitmask: Int = 0)

/**
 * Orders words by entry, then by string. Words that differ only in their bitmask compare equal.
 */
val WORD_COMPARATOR: Comparator<Word> = compareBy({ it.entry }, { it.string })

private val NON_LETTER_REGEX = Regex("[^\\p{L}]")

fun stringToEntry(string: String): String {
    return Normalizer.normalize(string, Normalizer.Form.NFKD)
        .replace(NON_LETTER_REGEX, "")
        .uppercase(Locale.ROOT)
        // There are some cases that need handling specially:
        .replace("Ø", "O")
}
