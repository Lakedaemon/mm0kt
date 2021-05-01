package org.mm0.kt

sealed class Matched {
    class Delimiters(val mm0: MM0.MM0Delimiters) : Matched()
    class Coercion(val mm0: MM0.MM0Coercion) : Matched()
    class Operator(val mm0: MM0.MM0Operator) : Matched()
    class Notation(val mm0: MM0.MM0Notation) : Matched()
    class Sort(val mm0: MM0.MM0Sort, val mmu: M.Computer.Sort) : Matched()
    class Term(val mm0: MM0.MM0Term, val mmu: M.Computer.Term) : Matched()
    class LocalDefinition(val mmu: M.Computer.Definition) : Matched()
    class Definition(val mm0: MM0.MM0Definition, val mmu: M.Computer.Definition) : Matched()
    class Axiom(val mm0: MM0.MM0Assert.MM0Axiom, val mmu: MMU.MMUAxiom) : Matched()
    class LocalTheorem(val mmu: MMU.MMUTheorem) : Matched()
    class Theorem(val mm0: MM0.MM0Assert.MM0Theorem, val mmu: MMU.MMUTheorem) : Matched()
    class Input(val mm0: MM0.MM0InOut, val mmu: MMU.MMUInput) : Matched()
    class Output(val mm0: MM0.MM0InOut, val mmu: MMU.MMUOutput) : Matched()
}