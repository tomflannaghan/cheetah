package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.words.Word

interface DefinitionSearcher {
    suspend fun lookupDefinition(word: Word): String
}