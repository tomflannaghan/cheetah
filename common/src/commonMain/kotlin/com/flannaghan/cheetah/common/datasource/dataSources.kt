package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File


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


suspend fun getAllWords(
    dataSources: List<DataSource>,
    context: ApplicationContext
): List<Word> = coroutineScope {
    // We want to group the words together, combining data sources.
    val allData = dataSources
        .map { async { it.getWords(context) } }
        .awaitAll()
        .flatten()
    val wordToDataSources = mutableMapOf<Pair<String, String>, List<DataSource>>()
    for (word in allData) {
        wordToDataSources[Pair(word.string, word.entry)] = wordToDataSources.getOrDefault(
            Pair(word.string, word.entry), listOf()
        ) + word.dataSources
    }

    wordToDataSources.map { Word(it.key.first, it.key.second, it.value) }
}