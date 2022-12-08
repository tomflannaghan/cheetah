package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word
import com.flannaghan.cheetah.common.words.WordComparatorByEntryAndString
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File


/**
 * A class that wraps up the management of multiple data sources. It caches loading of them etc.
 */
class DataSourcesManager(val dataSources: List<DataSource>) {
    private val dataSourceToBitMap = dataSources.withIndex().associate { Pair(it.value, 1 shl it.index) }

    private var dataSourcesProcessed = mutableSetOf<DataSource>()
    private var allWordsSorted = listOf<Word>()
    private val dataLock = Mutex(false)

    /**
     * Returns all words from the listed data sources.
     */
    suspend fun getAllWords(
        desiredDataSources: List<DataSource>,
        context: ApplicationContext
    ): List<Word> = coroutineScope {
        if (desiredDataSources.size == 0) return@coroutineScope listOf()
        // For any unprocessed dataSources, if we don't already have awaitables, create them.
        val comparator = WordComparatorByEntryAndString()
        val awaitables = mutableListOf<Deferred<Pair<DataSource, List<Word>>>>()
        dataLock.withLock {
            for (ds in desiredDataSources) {
                if (ds !in dataSourcesProcessed) {
                    val awaitable = async { Pair(ds, ds.getWords(context).sortedWith(comparator)) }
                    awaitables.add(awaitable)
                }
            }

            // Now await them outside of the lock.
            val allWordLists = awaitables.awaitAll()

            // Now integrate into the list if required. This is a merge.
            for ((ds, newWords) in allWordLists) {
                if (ds !in dataSourcesProcessed) {
                    val currentBitMask = dataSourceToBitMap[ds] ?: continue
                    val newAllWords = mutableListOf<Word>()
                    var i = 0
                    var j = 0
                    while (i < allWordsSorted.size && j < newWords.size) {
                        val currentWord = allWordsSorted[i]
                        val newWord = newWords[j]
                        val comparison = comparator.compare(currentWord, newWord)
                        if (comparison == -1) {
                            newAllWords.add(currentWord)
                            //println("${currentWord.string} not in ${ds.name}")
                            i++
                        } else if (comparison == 1) {
                            newAllWords.add(newWord.copy(bitmask = currentBitMask))
                            //println("${newWord.string} is new from ${ds.name}")
                            j++
                        } else {
                            // The two are equal.
                            newAllWords.add(currentWord.copy(bitmask = currentWord.bitmask or currentBitMask))
                            //println("${currentWord.string} is existing and also from ${ds.name}")
                            i++
                            j++
                        }
                    }
                    if (i < allWordsSorted.size) newAllWords.addAll(allWordsSorted.subList(i, allWordsSorted.size))
                    if (j < newWords.size) newAllWords.addAll(newWords.subList(j, newWords.size).map {
                        Word(it.string, it.entry, currentBitMask)
                    })
                    allWordsSorted = newAllWords
                    dataSourcesProcessed.add(ds)
                }
            }
        }

        // At this point, all words from the data sources will appear in allWordsSorted. Now just filter them down.
        val desiredBitMask = desiredDataSources.map { dataSourceToBitMap[it] }
            .filterNotNull()
            .reduce(Int::or)
        return@coroutineScope allWordsSorted.filter { (it.bitmask and desiredBitMask) != 0 }
    }

    fun getDataSources(bitmask: Int): List<DataSource> {
        return dataSourceToBitMap.filter { it.value and bitmask != 0 }.map { it.key }
    }
}


/**
 * Find all data sources defined in a directory (looks for .json files and treats them as configs).
 * Skips any that result in errors.
 */
fun dataSources(context: ApplicationContext): List<DataSource> {
    val results = mutableListOf<DataSource>()
    for (file in File(context.dataPath()).listFiles() ?: error("Root dir ${context.dataPath()} must be directory")) {
        if (file.isFile && file.name.endsWith(".json") && file.canRead()) {
            try {
                results.add(dataSourceFromJson(file))
            } catch (e: DataSourceDecodeError) {
                println("Decode error for $file: $e") // For now simply print error and move on!
            }
        }
    }
    return results
}
