package org.mm0.kt

interface Context {
    /** for the DynamicParser */
    val delimiters: STree<Delimiter>?
    fun operator(constant: CharSequence): M.Human.Operator?
    fun notation(constant: CharSequence): M.Human.Notation?
    fun directCoercion(actual: CharSequence, expected: CharSequence): List<M.Human.Coercion>?

    /** for proofs */
    fun term(id: CharSequence): M.Computer.Term?
    fun definition(id: CharSequence): M.Computer.Definition?
    fun assertion(id : CharSequence):M.Computer.Assertion?

    fun typeFor(id:CharSequence):Type?

    class Diff(val context1: Context, val context2: Context) :Context by context2 {
        val newNotations: MutableMap<String, M.Human.Notation> = mutableMapOf()
        val newOperators: MutableMap<String, M.Human.Operator> = mutableMapOf()
        val newDefinitions: MutableMap<String, M.Computer.Definition> = mutableMapOf()
        val hasDifferences: Boolean = context1 !== context2
    }
}