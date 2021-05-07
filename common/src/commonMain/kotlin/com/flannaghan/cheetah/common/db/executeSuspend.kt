package com.flannaghan.cheetah.common.db

import com.squareup.sqldelight.Query
import com.squareup.sqldelight.db.use
import kotlinx.coroutines.yield


/**
 * Behaves like executeAsList but reads the cursor and does the mapping in chunks of [chunkSize]. This means
 * the mapping of the query can be cancelled promptly.
 */
suspend fun <T : Any> Query<T>.executeAsListSuspend(chunkSize: Int = 1000): List<T> {
    val result = mutableListOf<T>()
    execute().use {
        var done = false
        while (!done) {
            for (i in 0 until chunkSize) {
                if (!it.next()) {
                    done = true
                    break
                } else {
                    result.add(mapper(it))
                }
            }
            yield()
        }
    }
    return result
}