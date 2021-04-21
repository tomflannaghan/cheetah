package com.flannaghan.cheetah.common

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

actual fun backgroundContext(): CoroutineContext = Dispatchers.Default
