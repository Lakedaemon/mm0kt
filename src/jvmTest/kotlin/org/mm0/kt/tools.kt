package org.mm0.kt

import java.io.File

actual fun string(folder:String, fileName:String):String= File(folder, fileName).readText(Charsets.US_ASCII)
actual fun filePathOf(folderPath: String): Sequence<String> = File(folderPath).listFiles().orEmpty().asSequence().map{it.absolutePath}