package org.mm0.kt

abstract class ProofChecker(val theorem: M.Computer.Assertion.Theorem, val binders:List<Binder>, val proof:CharSequence) {
    abstract fun check()
}