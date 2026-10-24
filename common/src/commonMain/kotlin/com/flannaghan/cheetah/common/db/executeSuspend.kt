package com.flannaghan.cheetah.common.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive


/**
 * Behaves like executeAsList but reads the cursor and does the mapping in chunks of [chunkSize], checking for
 * cancellation between chunks. This means the mapping of the query can be cancelled promptly.
 */
suspend fun <T : Any> Query<T>.executeAsListSuspend(chunkSize: Int = 1000): List<T> {
    val context = currentCoroutineContext()
    return execute { cursor ->
        val result = mutableListOf<T>()
        var done = false
        while (!done) {
            for (i in 0 until chunkSize) {
                if (!cursor.next().value) {
                    done = true
                    break
                } else {
                    result.add(mapper(cursor))
                }
            }
            context.ensureActive()
        }
        QueryResult.Value(result)
    }.value
}
