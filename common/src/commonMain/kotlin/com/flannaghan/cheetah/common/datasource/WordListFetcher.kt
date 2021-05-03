package com.flannaghan.cheetah.common.datasource

import com.flannaghan.cheetah.common.words.Word

interface WordListFetcher {
    suspend fun getWords(): List<Word>
}