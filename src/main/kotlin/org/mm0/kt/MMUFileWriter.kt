package org.mm0.kt

import java.io.File
import java.io.FileWriter

class MMUFileWriter(path:String) : MMUWriter() {
    private val fW = FileWriter(File(path))
    override fun write(string: String) {
        fW.write(string)
    }

    override fun close() {
        fW.close()
    }
}