package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.Platform
import com.flannaghan.cheetah.common.words.WORD_COMPARATOR
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A class that wraps up the management of multiple data sources. It caches loading of them etc.
 */
class DataSourcesManager(val dataSources: List<DataSource>) {
    init {
        require(dataSources.size <= Int.SIZE_BITS) { "At most ${Int.SIZE_BITS} data sources are supported" }
    }

    private val dataSourceToBitMask = dataSources.withIndex().associate { it.value to (1 shl it.index) }

    private val dataSourcesProcessed = mutableSetOf<DataSource>()
    private var allWordsSorted = listOf<Word>()
    private val dataLock = Mutex()

    /**
     * Returns all words from the listed data sources.
     */
    suspend fun getAllWords(desiredDataSources: List<DataSource>): List<Word> {
        if (desiredDataSources.isEmpty()) return emptyList()
        dataLock.withLock {
            // Load any data sources we haven't seen yet, in parallel, and merge them into the list.
            val newWordLists = coroutineScope {
                desiredDataSources
                    .filter { it !in dataSourcesProcessed }
                    .map { ds -> async { ds to ds.getWords().sortedWith(WORD_COMPARATOR) } }
                    .awaitAll()
            }
            for ((ds, newWords) in newWordLists) {
                val bitMask = dataSourceToBitMask[ds] ?: continue
                allWordsSorted = merge(allWordsSorted, newWords, bitMask)
                dataSourcesProcessed.add(ds)
            }
        }

        // At this point, all words from the data sources will appear in allWordsSorted. Now just filter them down.
        val desiredBitMask = desiredDataSources.mapNotNull { dataSourceToBitMask[it] }.fold(0, Int::or)
        return allWordsSorted.filter { (it.bitmask and desiredBitMask) != 0 }
    }

    fun getDataSources(bitmask: Int): List<DataSource> {
        return dataSourceToBitMask.filter { it.value and bitmask != 0 }.map { it.key }
    }
}

/**
 * Merges [newWords], which all come from the data source with [bitMask], into the sorted list [words].
 */
private fun merge(words: List<Word>, newWords: List<Word>, bitMask: Int): List<Word> {
    val result = ArrayList<Word>(words.size + newWords.size)
    var i = 0
    var j = 0
    while (i < words.size && j < newWords.size) {
        val word = words[i]
        val newWord = newWords[j]
        val comparison = WORD_COMPARATOR.compare(word, newWord)
        when {
            comparison < 0 -> {
                result.add(word)
                i++
            }

            comparison > 0 -> {
                result.add(newWord.copy(bitmask = bitMask))
                j++
            }

            else -> {
                result.add(word.copy(bitmask = word.bitmask or bitMask))
                i++
                j++
            }
        }
    }
    result.addAll(words.subList(i, words.size))
    newWords.subList(j, newWords.size).mapTo(result) { it.copy(bitmask = bitMask) }
    return result
}

/**
 * Finds all data sources defined in the data directory, treating every `.json` file as a config, in name order.
 * Skips any that are invalid.
 */
fun dataSources(platform: Platform): List<DataSource> {
    val files = platform.dataDir.listFiles() ?: error("Data dir ${platform.dataDir} must be a directory")
    return files
        .filter { it.isFile && it.extension == "json" && it.canRead() }
        .sortedBy { it.name }
        .mapNotNull {
            try {
                dataSourceFromJson(it, platform)
            } catch (e: InvalidDataSourceException) {
                System.err.println("Skipping data source: ${e.message}")
                null
            }
        }
}
