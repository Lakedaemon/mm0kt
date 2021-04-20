package org.mm0.kt

fun parsingBothPass() = passBoth("parsingBoth") {
    // mm0 and mmu syntax

    "tab is whitespace".test { sort("\t a\t") }
    "line feed is whitespace".test { sort("\u000A a\u000A") }
    "line tabulation is whitespace".test { sort("\u000B a\u000B") }
    "form feed is whitespace".test { sort("\u000C a\u000C") }
    "carriage return is whitespace".test { sort("\u000D a\u000D") }

    // int
    "big int support".test { op("plus", precedence = Int.MAX_VALUE - 1, constant = "+") }
    // delimiters
    "duplicate delimiters".test { both("(", "(") }
    "duplicate delimiters".test { leftRight("(", "(") }
    "duplicate delimiters".test { leftRight(right = listOf("(", "(")) }
}

fun matchingPass() {}
fun registeringPass() {}
fun proofCheckingPass() {}


fun pass() {
    parsingBothPass()
    matchingPass()
    registeringPass()
    proofCheckingPass()
}