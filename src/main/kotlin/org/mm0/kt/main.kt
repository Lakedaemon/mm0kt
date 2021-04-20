package org.mm0.kt

import java.io.File

fun string(path: String, ext:String) = File("$path$ext").readText(Charsets.US_ASCII)
fun proofCheck(path:String) = ContextBuilderImpl().asyncCheck(mm0SequenceOf(string(path, ".mm0"), simpleCanonizer), mmuSequenceOf(string(path, ".mmu"), simpleCanonizer), ::simpleCheck)
