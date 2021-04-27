package org.mm0.kt

import org.mm0.kt.M.Human.*
import org.mm0.kt.M.Computer.*

sealed class MM0 {
    /** mm0 specific, stuff for humans and the dynamic parser */
    data class MM0Delimiters(val delimiters: Delimiters, val comments: Comments) : MM0()
    data class MM0Coercion(val coercion: Coercion, val comments: Comments) : MM0()
    data class MM0Operator(val operator: Operator, val comments: Comments) : MM0()
    data class MM0Notation(val notation: Notation, val comments: Comments) : MM0()


    /** somewhat shared with mmu, must match */

    class MM0Sort(val sort: Sort, val comments: Comments) : MM0()
    class MM0Term(val id: String, val humanBinders: List<HumanBinder>, val arrows: List<Type>, val comments: Comments) : MM0()
    data class MM0Definition(val id: String, val humanBinders: List<HumanBinder>, val type: Type, val formula: CharSequence?, val comments: Comments) : MM0()
    sealed class MM0Assert(val id: String, val formulaTypeBinders: List<FormulaTypeBinder>, val arrows: List<FormulaOrType>, val comments: Comments) : MM0() {
        class MM0Theorem(id: String, formulaTypeBinders: List<FormulaTypeBinder>, arrows: List<FormulaOrType>, comments: Comments) : MM0Assert(id, formulaTypeBinders, arrows, comments)
        class MM0Axiom(id: String, formulaTypeBinders: List<FormulaTypeBinder>, arrows: List<FormulaOrType>, comments: Comments) : MM0Assert(id, formulaTypeBinders, arrows, comments)
    }

    class MM0InOut(val isInput: Boolean, val type: String, val ios: List<IO>, val comments: Comments) : MM0()
}

