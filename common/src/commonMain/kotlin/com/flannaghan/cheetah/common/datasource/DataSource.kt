package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.google.gson.Gson
import java.io.File
import java.util.*

/**
 * A data source can provide words and/or definitions for words. The word list element is required, but
 * the definition searching functionality is optional (e.g. a plain word list won't have definitions).
 * A color is also provided for the UI to display in an icon.
 */
data class DataSource(
    val name: String,
    val wordList: WordListFetcher,
    val definitionSearcher: DefinitionSearcher?,
    val color: Color
)


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


private data class DataSourceJson(
    val name: String,
    val type: String,
    val color: String,
    val metadata: Map<String, String>
)


open class DataSourceDecodeError(message: String) : Exception(message)


class UnknownTypeDataSourceError(type: String) : DataSourceDecodeError(type)
class FileDataSourceError(type: String) : DataSourceDecodeError(type)


fun dataSourceFromJson(jsonFile: File): DataSource {
    val json = jsonFile.readText()
    val gson = Gson()
    val dataSourceJson = gson.fromJson(json, DataSourceJson::class.java)
    val color = parseColor(dataSourceJson.color)
    return when (dataSourceJson.type) {
        "TextFileWordList" -> {
            val file = File(jsonFile.path.replace(".json", ".txt"))
            if (!file.exists() || !file.canRead()) throw FileDataSourceError(file.absolutePath)
            wordListTextFileDataSource(dataSourceJson.name, file, color)
        }
        "SqliteWordDatabase" -> {
            val file = File(jsonFile.path.replace(".json", ".sqlite"))
            if (!file.exists() || !file.canRead()) throw FileDataSourceError(file.absolutePath)
            sqliteWordDatabaseDataSource(dataSourceJson.name, file, color)
        }
        else -> throw UnknownTypeDataSourceError(dataSourceJson.type)
    }
}


class ParseColorError(color: String) : Exception(color)

fun parseColor(color: String): Color {
    val match = Regex("#([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})")
        .matchEntire(color.toUpperCase(Locale.ROOT)) ?: throw ParseColorError(color)
    val values = match.groupValues
    return Color(values[1].toInt(16), values[2].toInt(16), values[3].toInt(16))
}