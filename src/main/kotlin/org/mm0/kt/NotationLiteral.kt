package org.mm0.kt

sealed class NotationLiteral {
    class Constant(val constant: String, val precedence: Int = 0) : NotationLiteral() {
        override fun toString() = "($$constant$:$precedence)"
    }

    class ID(val id: String) : NotationLiteral() {
        override fun toString() = id
    }
}