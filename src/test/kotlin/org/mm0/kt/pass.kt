package org.mm0.kt


fun parsingPassForMM0() = passMM0("parsingMM0") {

}

fun parsingPassForMMU() = passMMU("parsingMMU") {

}

fun parsingPassForBoth() = passBoth("parsingBoth") {
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

fun matchingPass() = passBoth("matching") {
    // should I do all 16 possibilities ?
    "sort".test { sort("s") }
    "sort".test { sort("s", isPure = true) }
    "sort".test { sort("s", isProvable = true) }
    "sort".test { sort("s", isStrict = true) }
    "sort".test { sort("s", isFree = true) }
    "sort".test { sort("s", isFree = true, isStrict = true) }


    // term
    "same order for binders".test {
        sort("s")
        term("a", "s ()", "x s ()", "y s ()")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        term("a", "s ()", "x s ()", "y t ()")
    }
    "same order for binders".test {
        sort("s")
        mm0("term a (x y: s):s;")
        mmu("(term a ((x s ())(y s ())) (s ())")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        mm0("term a: s > t > s;")
        mmu("(term a ((y s ())(x t ())) (s ())")
    }
}

fun registeringPass() = passBoth("registering") {}

fun proofCheckingPass() = passBoth("proofChecking") {
    "trivial file".test { both("( )") }
}


fun pass() {
    parsingPassForMM0()
    parsingPassForMMU()
    parsingPassForBoth()
    matchingPass()
    registeringPass()
    proofCheckingPass()
}