package org.mm0.kt

fun parsingFailsForMM0() = failMM0("parsingMM0") {
    // int
    "negative int".test {
        comment("A lexeme is either one of the symbols, an identifier, a number (nonnegative integer), or a math string : number ::= 0 | [1-9][0-9]*")
        op("plus", precedence = -1, constant = "+")
    }

    "int prefixed with 0".test {
        comment("number ::= 0 | [1-9][0-9]*")
        raw("prefix p : $+$ prec 01;")
    }

    "abstract def with declared dummies".test {
        sort("s")
        term("a", "s")
        def("d", "s", null, "(.x:s)", tree = "a")
    }
}

fun parsingFailsForMMU() = failMMU("parsingMMU") {
    // local def

    // local theorem

}

fun parsingFailsForBoth() = failBoth("parsingBoth") {
    // ascii
    "next line is not ascii".test {
        sort("s\u0085")
    }
    "no-break-space is not ascii".test { sort("s\u00A0") }

    // whitespace
    "tab is not a valid whitespace".test {
        comment(" whitechar ::= ' ' | '\\n'")
        sort("s\t")
    }
    "line tabulation is not a valid whitespace".test {
        comment(" whitechar ::= ' ' | '\\n'")
        sort("s\u000B")
    }
    "form feed is not a valid whitespace".test {
        comment(" whitechar ::= ' ' | '\\n'")
        sort("s\u000C")
    }
    "carriage return is not a valid whitespace".test {
        comment(" whitechar ::= ' ' | '\\n'")
        sort("s\u000D")
    }


    // id
    "an id cannot be empty".test {
        comment(" identifier ::= [a-zA-Z_][a-zA-Z0-9_]*")
        sort("")
    }
    "an id cannot be blank".test {
        comment(" identifier ::= [a-zA-Z_][a-zA-Z0-9_]*")
        sort(" \n ")
    }
    "an id cannot start with digit".test {
        comment(" identifier ::= [a-zA-Z_][a-zA-Z0-9_]*")
        sort("0")
    }
    "bad char in id".test {
        comment("identifier ::= [a-zA-Z_][a-zA-Z0-9_]*")
        sort("sot-")
    }
    "a single underscore is not an id".test {
        comment(" the single character _ is not an identifier")
        sort("_")
    }

    // formula
    "dollar cannot appear in formula".test {
        comment("Inside a math string \$ cannot appear : math-string ::= '\$' [^\\\$]* '\$'")
        both("a $ b")
    }
    "dollar cannot appear in formula".test {
        comment("Inside a math string \$ cannot appear : math-string ::= '\$' [^\\\$]* '\$'")
        leftRight("a $ b")
    }
    "dollar cannot appear in formula".test {
        comment("Inside a math string \$ cannot appear : math-string ::= '\$' [^\\\$]* '\$'")
        leftRight(right = listOf("a $ b"))
    }







    "garbage after last statement".test {
        sort("s")
        raw(" garbage ")
    }

    // delimiters


    // term

}


fun matchingFails() = failBoth("matching") {
//term

    "different binders order".test {
        sort("s")
        sort("t")
        mm0("term a: s > t > s;")
        mmu("(term a ((y t ())(x s ())) (s ())")
    }
}

fun registeringFails() = failBoth("registering") {
    // sort
    "duplicated ids for sorts".test {
        sort("s", false)
        sort("s", true)
    }

    // pure means that this sort does not have any term formers. It is an uninterpreted domain which may have variables but has no constant symbols, binary operators, or anything else targeting this sort. If a sort has this modifier, it is illegal to declare a term with this sort as the target.
    "term typed with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s")
    }


    // strict is the "opposite" of pure: it says that the sort does not have any variable binding operators. It is illegal to have a bound variable or dummy variable of this sort, and it cannot appear as a dependency in another variable. For example, if x: set and ph: wff x then set must not be declared strict. (pure and strict are not mutually exclusive, although a sort with both properties is not very useful.)
    "dummy variable with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s", "{x:s}")
    }

    "dependency with a pure sort".test {
        sort("s", isPure = true)
        term("a", "s x", "(x:s)")
    }
    "assertion formula without provable sort".test {
        sort("s")
        term("a", "s")
        comment("""provable means that the sort is a thing that can be "proven". All formulas appearing in axioms and theorems (between $) must have a provable sort""")
        axiom("b", "$ a $", "(x:s)")
        //mm0("axiom b (x:s): $ a $;")
    }

    "term typed with a pure sort".test {
        sort("s", isFree = true)
        term("f", "s", "{x:s}")
        comment("free means that dummy variables may not be dropped in definitions unless they appear in binding syntax constructors.")
        def("y", "s", "f x", "(.x:s)"/*, tree="(f x)"*/)
    }

    // coercion
    // GOT ME
    "duplicated ids for coercion".test {
        sort("s")
        sort("t")
        sort("u")
        coercion("c", "s", "t")
        coercion("c", "s", "u")
    }


    "not unique coercion path".test {
        sort("s")
        sort("t")
        sort("u")
        coercion("c1", "s", "t")
        coercion("c2", "t", "u")
        coercion("c3", "s", "u")
    }

    "duplicated id for terms".test {
        sort("s")
        sort("t")
        term("a", "s")
        term("a", "t")
    }

    "term with non-existent sort".test { term("a", "s") }

    "term typed with a dummy".test {
        sort("s")
        mm0("term a:s;")
        mmu("(term a () (s))")
    }

    "term shadowing a def".test {
        sort("s")
        term("a", "s")
        def("b", "s", "a")
        term("b", "s")
    }

    // def
    "def shadowing a term".test {
        sort("s")
        term("a", "s")
        term("b", "s")
        def("a", "s", "b")
    }

    "recursive def".test {
        sort("s")
        def("a", "s", "a ", tree = "a")
    }
}

fun dynamicParsingFails() = passBoth("dynamic parsing") {}

fun proofCheckingFails() = passBoth("proof checking"){}

fun fail() {
    parsingFailsForMM0()
    parsingFailsForMMU()
    parsingFailsForBoth()
    matchingFails()
    registeringFails()
    dynamicParsingFails()
    proofCheckingFails()
}