package org.mm0.kt

interface TestWriter {
    /** delimiters */
    fun both(vararg both: String)
    fun leftRight(vararg left: String, right: List<String> = listOf())
    fun sort(id: String="s", isPure: Boolean = false, isStrict: Boolean = false, isProvable: Boolean = false, isFree: Boolean = false)
    fun coercion(id: String = "id${idCount++}", coerced: String, coercedInto: String)
    fun term(id: String  = "id${idCount++}", type: String ="s ()", vararg binders: String)
    fun op(id: String = "id${idCount++}", constant: String ="+", precedence: Int=10, opType: String = PREFIX)
    fun def(id: String = "id${idCount++}", type: String = "s ()", tree: String, vararg binders: String, moreDummies: String = "")

    fun raw(string:String)
    companion object {
        private var idCount = 0
    }
}

interface TestWriterBoth :TestWriter{
    fun mm0(string:String)
    fun mmu(string:String)
}