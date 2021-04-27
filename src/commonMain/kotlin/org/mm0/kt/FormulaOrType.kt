package org.mm0.kt

sealed class FormulaOrType {
    class Formula(val formula: String) : FormulaOrType()
    class Type(val type: org.mm0.kt.Type) : FormulaOrType()
}