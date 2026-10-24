package com.flannaghan.cheetah.common.datasource

import androidx.compose.ui.graphics.Color
import com.flannaghan.cheetah.common.Platform
import com.flannaghan.cheetah.common.words.Word
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.util.*

/**
 * A data source can provide words and/or definitions for words. The base class just defines getting words.
 * A color is also provided for the UI to display in an icon.
 */
abstract class DataSource(val name: String, val color: Color, val defaults: DataSourceDefaults) {
    abstract suspend fun getWords(): List<Word>
}

/**
 * A data source that can also look up and search definitions.
 */
abstract class DefinitionDataSource(name: String, color: Color, defaults: DataSourceDefaults) :
    DataSource(name, color, defaults) {
    abstract suspend fun lookupDefinition(word: Word): String
    abstract suspend fun fullTextSearch(allWords: List<Word>, pattern: String): List<Word>
    abstract suspend fun findLinkedWords(allWords: List<Word>, word: String): List<Word>
}

data class DataSourceDefaults(val useWordList: Boolean, val useDefinitions: Boolean)

/**
 * The contents of a data source's `.json` config file.
 */
@Serializable
private data class DataSourceConfig(
    val name: String,
    val type: String,
    val color: String,
    val useWordListByDefault: Boolean = true,
    val useDefinitionsByDefault: Boolean = true,
)

private val configJson = Json { ignoreUnknownKeys = true }

class InvalidDataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Reads a data source config. Its data file is the sibling file with the same name and the type's extension.
 */
fun dataSourceFromJson(jsonFile: File, platform: Platform): DataSource {
    val config = try {
        configJson.decodeFromString<DataSourceConfig>(jsonFile.readText())
    } catch (e: SerializationException) {
        throw InvalidDataSourceException("Invalid config $jsonFile: ${e.message}", e)
    }
    val color = parseColor(config.color)
    val defaults = DataSourceDefaults(config.useWordListByDefault, config.useDefinitionsByDefault)

    fun dataFile(extension: String): File {
        val file = jsonFile.resolveSibling("${jsonFile.nameWithoutExtension}.$extension")
        if (!file.canRead()) throw InvalidDataSourceException("Can't read $file")
        return file
    }

    return when (config.type) {
        "TextFileWordList" -> WordListTextFileDataSource(config.name, dataFile("txt"), color, defaults)
        "SqliteWordDatabase" ->
            SqliteWordDatabaseDataSource(config.name, dataFile("sqlite"), color, defaults, platform)

        else -> throw InvalidDataSourceException("Unknown data source type ${config.type} in $jsonFile")
    }
}

private val COLOR_REGEX = Regex("#([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})")

fun parseColor(color: String): Color {
    val match = COLOR_REGEX.matchEntire(color.uppercase(Locale.ROOT))
        ?: throw InvalidDataSourceException("Invalid color $color")
    val (red, green, blue) = match.destructured
    return Color(red.toInt(16), green.toInt(16), blue.toInt(16))
}
