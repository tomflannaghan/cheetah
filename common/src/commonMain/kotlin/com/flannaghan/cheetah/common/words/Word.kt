package com.flannaghan.cheetah.common.words

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.Normalizer
import java.util.*

/**
 * A word. It has a String representation, and also an Ascii crossword entry that we use for matching.
 */
data class Word(val string: String, val entry: String) {
    constructor(string: String) : this(string, stringToEntry(string))
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

/**
 * Takes a string with a word on each line, and converts them all to word instances. This is more efficient
 * than doing it one word at a time.
 */
fun stringsToWords(strings: Collection<String>): List<Word> {
    val matcher = LETTER_REGEX.toPattern().matcher("")
    val words = mutableListOf<Word>()
    for (string in strings) {
        var entry = Normalizer.normalize(string, Normalizer.Form.NFKD)
            .toUpperCase(Locale.ROOT)
            .replace("Ø", "O")
        // Now remove all control characters.
        matcher.reset(entry)
        entry = matcher.replaceAll("")
        if (entry == "") continue
        words.add(Word(string, entry))
    }
    return words
}

/**
 * A parallel version of stringToWords.
 */
suspend fun stringsToWordsParallel(strings: Collection<String>): List<Word> = coroutineScope {
    strings
        .chunked(10000)
        .map {
            async { stringsToWords(it) }
        }
        .awaitAll()
        .flatten()
}