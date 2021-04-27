package org.mm0.kt

import java.io.File


actual fun filePathOf(folderPath: String): Sequence<String> = File(folderPath).listFiles().orEmpty().asSequence().map{it.absolutePath}