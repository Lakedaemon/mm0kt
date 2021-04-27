package org.mm0.kt

fun patch(folder:String, name:String, newFolder:String, newName:String) {
    // first parse the old stuff
    val contextBuilder = ContextBuilderImpl()
    contextBuilder.asyncCheck(mm0SequenceOf(string("$name.mm0", folder), simpleCanonizer), mmuSequenceOf(string("$name.mmu", folder), simpleCanonizer), ::simpleCheck)
    // get the ProofResults
    // get the mixed list


}