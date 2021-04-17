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
}