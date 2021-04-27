package org.mm0.kt

sealed class FormulaTypeBinder(val isBound: Boolean, val names: List<String>) {
    class Formula(isBound: Boolean, names: List<String>, val formula: String) : FormulaTypeBinder(isBound, names)
    class Type(isBound: Boolean, names: List<String>, val type: org.mm0.kt.Type) : FormulaTypeBinder(isBound, names)
}