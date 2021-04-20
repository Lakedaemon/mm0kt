package org.mm0.kt

interface TestWriter {
    /** delimiters */
    fun both(vararg both: String)
    fun leftRight(vararg left: String, right: List<String> = listOf())
    fun sort(id: String, isPure: Boolean = false, isStrict: Boolean = false, isProvable: Boolean = false, isFree: Boolean = false)
    fun coercion(id: String, coerced: String, coercedInto: String)
    fun term(id: String, type: String, vararg binders: String, mm0Declaration: String = "")
    fun op(id: String, constant: String, precedence: Int, opType: String = PREFIX)
    fun def(id: String, type: String, tree: String, vararg binders: String, moreDummies: String = "", mm0Declaration: String = "")
}