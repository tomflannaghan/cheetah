package com.flannaghan.cheetah.common

import java.io.InputStream

/**
 * A context encapsulating the functionality exposed by the OS that may depend on the application instance.
 * For android, this wraps functionality requiring access to the Context of the application.
 */
interface ApplicationContext {
    fun openFile(filename: String): InputStream
}
