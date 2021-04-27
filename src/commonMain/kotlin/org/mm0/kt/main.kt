package org.mm0.kt


expect fun string(folder: String, fileName: String): String
fun proofCheck(path:String) = ContextBuilderImpl().asyncCheck(mm0SequenceOf(string(path, ".mm0"), simpleCanonizer), mmuSequenceOf(string(path, ".mmu"), simpleCanonizer), ::simpleCheck)
