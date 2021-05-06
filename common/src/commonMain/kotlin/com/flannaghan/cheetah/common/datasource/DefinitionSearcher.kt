package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.ApplicationContext
import com.flannaghan.cheetah.common.words.Word

interface DefinitionSearcher {
    suspend fun lookupDefinition(context: ApplicationContext, word: Word): String
    suspend fun fullTextSearch(context: ApplicationContext, allWords: List<Word>, pattern: String): List<Word>
}