package com.flannaghan.cheetah.common

import kotlin.coroutines.CoroutineContext

expect fun getPlatformName(): String

expect fun backgroundContext(): CoroutineContext