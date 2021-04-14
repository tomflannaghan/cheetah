package com.flannaghan.cheetah.common

import com.flannaghan.cheetah.common.words.FileWordSource
import com.flannaghan.cheetah.common.words.WordSource

fun wordSources(context: ApplicationContext): List<WordSource> {
    return listOf(
        FileWordSource("UKACD", context.openFile("UKACD17.txt").reader(Charsets.UTF_8)),
    )
}