package org.mm0.kt

fun parsingDualPass(){
    // mm0 and mmu syntax
    "tab is whitespace".pass { sort("\t a\t") }
    "line feed is whitespace".pass { sort("\u000A a\u000A") }
    "line tabulation is whitespace".pass { sort("\u000B a\u000B") }
    "form feed is whitespace".pass { sort("\u000C a\u000C") }
    "carriage return is whitespace".pass { sort("\u000D a\u000D") }

    // int
    "big int support".pass { op("plus", precedence = Int.MAX_VALUE - 1, constant = "+") }
    // delimiters
    "duplicate delimiters".pass { both("(", "(") }
    "duplicate delimiters".pass { leftRight("(", "(") }
    "duplicate delimiters".pass { leftRight(right = listOf("(", "(")) }
}
fun matchingPass(){}
fun registeringPass(){}
fun proofCheckingPass() {}


fun pass() {
    parsingDualPass()
    matchingPass()
    registeringPass()
    proofCheckingPass()
}