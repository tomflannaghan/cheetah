package com.flannaghan.cheetah.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual fun getPlatformName(): String {
    return "Android"
}

actual fun backgroundContext(): CoroutineContext = Dispatchers.IO
