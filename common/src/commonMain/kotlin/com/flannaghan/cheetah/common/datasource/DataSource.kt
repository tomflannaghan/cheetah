package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word
import com.google.gson.Gson
import java.io.File
import java.util.*

/**
 * A data source can provide words and/or definitions for words. The base class just defines getting words.
 * A color is also provided for the UI to display in an icon.
 */
abstract class DataSource(val name: String, val color: Color, val defaults: DataSourceDefaults) {
    abstract suspend fun getWords(context: ApplicationContext): List<Word>
}

/* DefinitionDataSource adds the ability to search definitions */
abstract class DefinitionDataSource(name: String, color: Color, defaults: DataSourceDefaults) :
    DataSource(name, color, defaults) {
    abstract suspend fun lookupDefinition(context: ApplicationContext, word: Word): String
    abstract suspend fun fullTextSearch(context: ApplicationContext, allWords: List<Word>, pattern: String): List<Word>
    abstract suspend fun findLinkedWords(context: ApplicationContext, allWords: List<Word>, word: String): List<Word>
}


data class DataSourceDefaults(val useWordList: Boolean, val useDefinitions: Boolean)


private data class DataSourceJson(
    val name: String,
    val type: String,
    val color: String,
    val metadata: Map<String, String>,
    val useWordListByDefault: Boolean?,
    val useDefinitionsByDefault: Boolean?,
)


open class DataSourceDecodeError(message: String) : Exception(message)


class UnknownTypeDataSourceError(type: String) : DataSourceDecodeError(type)
class FileDataSourceError(type: String) : DataSourceDecodeError(type)


fun dataSourceFromJson(jsonFile: File): DataSource {
    val json = jsonFile.readText()
    val gson = Gson()
    val dataSourceJson = gson.fromJson(json, DataSourceJson::class.java)
    val color = parseColor(dataSourceJson.color)
    val defaults = DataSourceDefaults(
        dataSourceJson.useWordListByDefault ?: true,
        dataSourceJson.useDefinitionsByDefault ?: true
    )
    return when (dataSourceJson.type) {
        "TextFileWordList" -> {
            val file = File(jsonFile.path.replace(".json", ".txt"))
            if (!file.exists() || !file.canRead()) throw FileDataSourceError(file.absolutePath)
            WordListTextFileDataSource(dataSourceJson.name, file, color, defaults)
        }
        "SqliteWordDatabase" -> {
            val file = File(jsonFile.path.replace(".json", ".sqlite"))
            if (!file.exists() || !file.canRead()) throw FileDataSourceError(file.absolutePath)
            SqliteWordDatabaseDataSource(dataSourceJson.name, file, color, defaults)
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