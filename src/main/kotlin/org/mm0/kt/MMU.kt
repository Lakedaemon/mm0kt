package org.mm0.kt

import org.mm0.kt.M.Computer.*
import org.mm0.kt.M.Computer.Assertion.*

sealed class MMU {
    class MMULineComment(val lineComment: M.Human.LineComment): MMU()
    class MMUSort(val sort:Sort) : MMU()
    class MMUTerm(val term:Term) : MMU()
    class MMUDefinition(val definition:Definition):MMU()
    class MMUInput(val input:Input) : MMU()
    class MMUOutput(val output: Output) : MMU()
    class MMUAxiom(val axiom:Axiom):MMU()
    class MMUTheorem(val theorem:Theorem, val binders:List<Binder>, val proof: CharSequence) :MMU()

}


