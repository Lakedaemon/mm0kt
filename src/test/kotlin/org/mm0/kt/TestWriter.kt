package org.mm0.kt


/**
 * it would be nice for term, def, axiom, theorem
 * to input the mm0 stuff and to convert it into mmu stuff (half the work)
 *
 * Yet, for def/axiom/theorem, it requires
 * to register stuff in a ContextBuilder (which means turning term into mmu mm0-things into mmu stuff)
 * and then to use a dynamic parser to turn complex mm0 stuff into mmu and register it
 *
 * sounds doable but complex and time consuming (for tests :/)
 *
 * we should NOT parse mm0 stuff (else it will fail with bad things)
 *
 * we can use that for pass things, but not registering things (except if we delay the last registering step)
 *
 * if we manage to build mmu stuff, we can then use the rewriter stuff
 * This sounds great, except for bad/lone mmu stuff and bad mm0 stuff
 *
 * */
interface TestWriter {
    /** delimiters */
    fun comment(vararg strings:String)
    fun both(vararg both: String)
    fun leftRight(vararg left: String, right: List<String> = listOf())
    fun sort(id: String="s", isPure: Boolean = false, isStrict: Boolean = false, isProvable: Boolean = false, isFree: Boolean = false)
    fun coercion(id: String = "id${idCount++}", coerced: String, coercedInto: String)
    fun term(id: String  = "id${idCount++}", arrows: String ="s", vararg humanBinders: String)
    fun op(id: String = "id${idCount++}", constant: String ="+", precedence: Int=10, opType: String = PREFIX)

    fun def(id: String = "id${idCount++}", type: String = "s", formula: String?, vararg humanBinders: String, tree:String=formula?:"", isLocal: Boolean=false)
    fun axiom(id:String= "id${idCount++}", arrows:String, vararg formulaTypeBinders:String)
    fun theorem(id: String, arrows: String, vararg formulaTypeBinders: String, proof:String, isLocal:Boolean=false)

    /** standardize on mm0*/


    fun raw(string:String)
    companion object {
        private var idCount = 0
    }
}

interface TestWriterBoth :TestWriter{
    fun mm0(string:String)
    fun mmu(string:String)
}