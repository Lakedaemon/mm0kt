package org.mm0.kt

sealed class IO {
    class Formula(val formula: String) : IO()
    class ID(val id: String) : IO()
}