package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.words.Word

/**
 * Provides a context for sharing info between searches.
 */
class SearchContext(
    val words: List<Word>,
    val fullTextSearch: (suspend (String, List<Word>) -> List<Word>)? = null
)