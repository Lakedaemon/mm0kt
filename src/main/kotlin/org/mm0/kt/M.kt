package org.mm0.kt

sealed class M {
    sealed class Human : M() {
        data class Delimiters(val left:List<String>, val both: List<String>, val right:List<String>):Human()
        data class Coercion(val id: String, val coerced: String, val coercedInto: String) : Human()

        /** notational constructs */
        data class Notation(val id: String, val humanBinders: List<HumanBinder>, val type: Type, val constant: String, val precedence: Int, val notationLiterals: List<NotationLiteral>) : Human()

        /** multi-ary (prefix) and binary (infixl & infixr) operators */
        data class Operator(val id: String, val constant: String, val precedence: Int, val operatorType: String) : Human()
    }

    sealed class Computer : M() {
        data class Term(val id: String, val binders: List<Binder>, val type: Type) : Computer()
        data class Definition(val id: String, val isLocal: Boolean, val binders: List<Binder>, val type: Type, val moreDummiesForDef: List<Binder>, val tree: StringTree) : Computer()
        data class Sort(val id: String, val isPure: Boolean, val isStrict: Boolean, val isProvable: Boolean, val isFree: Boolean) : Computer()
        data class Input(val type: String) : Computer()
        data class Output(val type: String) : Computer()
        sealed class Assertion(val id: String, val binders: List<Binder>, val hypotheses: List<NamedHypothesis>, val conclusion: StringTree) : Computer() {
            class Theorem(id: String, binders: List<Binder>, hypotheses: List<NamedHypothesis>, conclusion: StringTree, val isLocal: Boolean) : Assertion(id, binders, hypotheses, conclusion)
            class Axiom(id: String, binders: List<Binder>, hypotheses: List<NamedHypothesis>, conclusion: StringTree) : Assertion(id, binders, hypotheses, conclusion)
        }
    }
}