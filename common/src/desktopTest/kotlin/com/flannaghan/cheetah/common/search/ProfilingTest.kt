package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.DesktopPlatform
import com.flannaghan.cheetah.common.datasource.DataSourcesManager
import com.flannaghan.cheetah.common.datasource.dataSources
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.minutes

/**
 * This isn't really a test, rather a set of benchmarks for profiling/keeping track of performance. Making them
 * tests means we check they run correctly.
 */
class ProfilingTest {
    @Test
    fun search() = runTest(timeout = 5.minutes) {
        val manager = DataSourcesManager(dataSources(DesktopPlatform()))
        val context = SearchContext(manager.getAllWords(manager.dataSources.filter { it.defaults.useWordList }))
        assertTrue(search("z", context).success)
        // All the time is in the prefix tree construction. The second run is fast.
        val time1 = measureTimeMillis { context.withEvaluation { search("><", context) } }
        val time2 = measureTimeMillis { context.withEvaluation { search("<>", context) } }
        println("Time: $time1 $time2")
    }
}
