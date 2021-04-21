package com.flannaghan.cheetah.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual fun getPlatformName(): String {
    return "Desktop"
}

actual fun backgroundContext(): CoroutineContext = Dispatchers.Default
