package com.flannaghan.cheetah.common.search

import com.flannaghan.cheetah.common.DesktopApplicationContext
import com.flannaghan.cheetah.common.DesktopSearchModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * This isn't really a test, rather a set of benchmarks for profiling/keeping track of performance. Making them
 * tests means we check they run correctly.
 */
class ProfilingTest {
    @Test
    fun search() = runBlocking {
        val context = DesktopApplicationContext()
        val model = DesktopSearchModel(context, this)
        val time1 = measureTimeMillis { model.doSearch("><") }
        val time2 = measureTimeMillis { model.doSearch("<>") }
        println("Time: $time1 $time2")  // All the time is in the prefix tree construction. The second run is fast.
    }
}