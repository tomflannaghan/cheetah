package com.flannaghan.cheetah.common

import java.io.FileInputStream
import java.io.InputStream

class DesktopApplicationContext : ApplicationContext {
    override fun openFile(filename: String): InputStream {
        return FileInputStream("/home/tom/src/cheetah/data/$filename")
    }
}