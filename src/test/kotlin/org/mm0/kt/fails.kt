package org.mm0.kt

fun parsingFailsForMM0() = failMM0("parsingMM0") {
    // int
    "ints cannot be negative".test {
        comment("A lexeme is either one of the symbols, an identifier, a number (nonnegative integer), or a math string : number ::= 0 | [1-9][0-9]*")
        op("plus", precedence = -1, constant = "+")
    }

    "int cannot be prefixed with zeros".test {
        comment("number ::= 0 | [1-9][0-9]*")
        raw("prefix p : $+$ prec 01;")
    }

    "abstract definitions cannot be declared  with dummies".test {
        sort("s")
        term("a", "s")
        def("d", "s", null, "(.x:s)", tree = "a")
    }

    // formula
    "bad-math-string".test {
        comment("        --V one \$ too many")
        both("$")
    }
    "bad-math-string".test {
        comment("        --V one \$ too many")
        leftRight("$")
    }
}

fun parsingFailsForMMU() = failMMU("parsingMMU") {
    // local def

    // local theorem

}




fun parsingFailsForBoth() = failBoth("parsingBoth") {
    // shipped
    "chars cannot be outside a subset of the ascii charset".test {
        comment("lexeme ::= symbol | identifier | number | math-string",
                "symbol ::= '*' | '.' | ':' | ';' | '(' | ')' | '>' | '{' | '}' | '=' | '_'",
                "identifier ::= [a-zA-Z_][a-zA-Z0-9_]*",
                "number ::= 0 | [1-9][0-9]*",
                "math-string ::= '$' [^\$]* '$'", mmu = listOf("Whitespace is ignored except to separate tokens",
                                                               "Line comments are written --comment and extend until the next \\n; line comments act like whitespace",
                                                               "An identifier token matches [0-9a-zA-Z_:]+",
                                                               "Characters ( ) are single character symbol tokens",
                                                               "Anything else is forbidden"))
        sort("s\u0085")
    }
    "chars cannot be outside a subset of the ascii charset".test {
        comment("lexeme ::= symbol | identifier | number | math-string",
                "symbol ::= '*' | '.' | ':' | ';' | '(' | ')' | '>' | '{' | '}' | '=' | '_'",
                "identifier ::= [a-zA-Z_][a-zA-Z0-9_]*",
                "number ::= 0 | [1-9][0-9]*",
                "math-string ::= '$' [^\$]* '$'", mmu = listOf("Whitespace is ignored except to separate tokens",
                                                               "Line comments are written --comment and extend until the next \\n; line comments act like whitespace",
                                                               "An identifier token matches [0-9a-zA-Z_:]+",
                                                               "Characters ( ) are single character symbol tokens",
                                                               "Anything else is forbidden"))
        sort("s\u00A0")
    }

    // shipped
    // chars
    "bad-whitespace".test {
        comment("    v-- this is a TAB", mmu=listOf("     v-- this is a TAB"))
        sort("s\t")
    }
    "bad-whitespace".test {
        comment("    v-- this is a LINE TABULATION", mmu=listOf("     v-- this is a LINE TABULATION"))
        sort("s\u000B")
    }
    "bad-whitespace".test {
        comment("    v-- this is a FORM FEED", mmu=listOf("     v-- this is a FORM FEED"))
        sort("s\u000C")
    }
    "bad-whitespace".test {
        comment("    v-- this is a CARRIAGE RETURN", mmu=listOf("     v-- this is a CARRIAGE RETURN"))
        sort("s\u000D")
    }

    // shipped
    // id
    "bad-id".test {
        comment(" empty id", mmu=listOf(" empty id"))
        sort("")
    }
    "bad-id".test {
        comment(" blank id", mmu=listOf(" blank id"))
        sort(" \n ")
    }
    "bad-id".test {
        comment(" id starting with a digit", mmu=listOf(" id starting with a digit"))
        sort("0")
    }
    "bad-id".test {
        comment("id with forbidden char -", mmu=listOf("id with forbidden char -"))
        sort("sot-")
    }
    "bad-id".test {
        comment(" id that is a single underscore _", mmu=listOf(" id that is a single underscore _"))
        sort("_")
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
    }

    "term typed with a pure sort".test {
        sort("s", isFree = true)
        term("f", "s", "{x:s}")
        comment("free means that dummy variables may not be dropped in definitions unless they appear in binding syntax constructors.")
        def("y", "s", "f x", "(.x:s)")
    }

    // coercion
    // GOT ME
    /*"duplicated ids for coercion".test {
        sort("s")
        sort("t")
        sort("u")
        coercion("c", "s", "t")
        coercion("c", "s", "u")
    }*/


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

fun proofCheckingFails() = passBoth("proof checking") {}

fun fail() {
    parsingFailsForMM0()
    parsingFailsForMMU()
    parsingFailsForBoth()
    matchingFails()
    registeringFails()
    dynamicParsingFails()
    proofCheckingFails()
}