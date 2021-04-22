package org.mm0.kt

import org.mm0.kt.M.Human.*
import org.mm0.kt.M.Computer.*

sealed class MM0 {
    /** mm0 specific, stuff for humans and the dynamic parser */
    class MM0LineComment(val lineComment:LineComment): MM0()
    data class MM0Delimiters(val delimiters: Delimiters) : MM0()
    data class MM0Coercion(val coercion: Coercion) : MM0()
    data class MM0Operator(val operator: Operator) : MM0()
    data class MM0Notation(val notation: Notation) : MM0()


    /** somewhat shared with mmu, must match */

    class MM0Sort(val sort: Sort) : MM0()
    class MM0Term(val id: String, val humanBinders: List<HumanBinder>, val arrows: List<Type>) : MM0()
    data class MM0Definition(val id: String, val humanBinders: List<HumanBinder>, val type: Type, val formula: CharSequence?) : MM0()
    sealed class MM0Assert(val id: String, val formulaTypeBinders: List<FormulaTypeBinder>, val arrows: List<FormulaOrType>) : MM0() {
        class MM0Theorem(id: String, formulaTypeBinders: List<FormulaTypeBinder>, arrows: List<FormulaOrType>) : MM0Assert(id, formulaTypeBinders, arrows)
        class MM0Axiom(id: String, formulaTypeBinders: List<FormulaTypeBinder>, arrows: List<FormulaOrType>) : MM0Assert(id, formulaTypeBinders, arrows)
    }

    class MM0InOut(val isInput: Boolean, val type: String, val ios: List<IO>) : MM0()
}

