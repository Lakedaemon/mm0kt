package org.mm0.kt

fun parsingFailsForMM0() = failMM0("parsingMM0") {
    // int number ::= 0 | [1-9][0-9]*

    "bad-int".test {
        comment("          V--  overflowing hazard")
        raw("prefix p : $+$ prec 4294967296;")
    }

    "bad-int".test {
        comment("          V--  not an int")
        raw("prefix p : $+$ prec 1.0;")
    }

    "bad-int".test {
        comment("          V--  forbidden sign")
        raw("prefix p : $+$ prec +1;")
    }

    "bad-int".test {
        comment("          V--  forbidden sign")
        op("plus", precedence = -1, constant = "+")
    }

    "bad-int".test {
        comment("              V--  forbidden 0 prefix -")
        raw("prefix p : $+$ prec 01;")
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



    "abstract definitions cannot be declared  with dummies".test {
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
        comment("    v-- this is a TAB", mmu = listOf("     v-- this is a TAB"))
        sort("s\t")
    }
    "bad-whitespace".test {
        comment("    v-- this is a LINE TABULATION", mmu = listOf("     v-- this is a LINE TABULATION"))
        sort("s\u000B")
    }
    "bad-whitespace".test {
        comment("    v-- this is a FORM FEED", mmu = listOf("     v-- this is a FORM FEED"))
        sort("s\u000C")
    }
    "bad-whitespace".test {
        comment("    v-- this is a CARRIAGE RETURN", mmu = listOf("     v-- this is a CARRIAGE RETURN"))
        sort("s\u000D")
    }

    // shipped
    // id
    "bad-id".test {
        comment(" empty id", mmu = listOf(" empty id"))
        sort("")
    }
    "bad-id".test {
        comment(" blank id", mmu = listOf(" blank id"))
        sort(" \n ")
    }
    "bad-id".test {
        comment(" id starting with a digit", mmu = listOf(" id starting with a digit"))
        sort("0")
    }
    "bad-id".test {
        comment("id with forbidden char -", mmu = listOf("id with forbidden char -"))
        sort("sot-")
    }
    "bad-id".test {
        comment(" id that is a single underscore _", mmu = listOf(" id that is a single underscore _"))
        sort("_")
    }


    val mod = arrayOf(PURE, STRICT, PROVABLE, FREE)
// sort-stmt ::= ('pure')? ('strict')? ('provable')? ('free')? 'sort' identifier ';'
    for (a in 0..3) for (b in 0..3) if (a> b) "bad-sort${a+1}${b+1}".test {
        comment("${mod[a]} should be after ${mod[b]}", mmu=listOf("${mod[a]} should be after ${mod[b]}"))
        mm0("${mod[a]} ${mod[b]} sort s${a+1}${b+1};")
        mmu("(sort s${a+1}${b+1} ${mod[a]} ${mod[b]})")
    }

    for (a in 0..3) for (b in 0..3) for (c in 0..3) if (setOf(a,b,c).size==3 && (a>b || b> c)) "bad-sort${a+1}${b+1}${c+1}".test {
        comment("bad modifier order", mmu=listOf("bad modifier order"))
        mm0("${mod[a]} ${mod[b]} ${mod[c]} sort s${a+1}${b+1}${c+1};")
        mmu("(sort s${a+1}${b+1}${c+1} ${mod[a]} ${mod[b]} ${mod[c]})")
    }

    for (a in 0..3) for (b in 0..3) for (c in 0..3) for (d in 0..3) if (setOf(a,b,c,d).size==4 && (a>b || b> c|| c > d)) "bad-sort${a+1}${b+1}${c+1}${d+1}".test {
        comment("bad modifier order", mmu=listOf("bad modifier order"))
        mm0("${mod[a]} ${mod[b]} ${mod[c]} ${mod[d]} sort s${a+1}${b+1}${c+1}${d+1};")
        mmu("(sort s${a+1}${b+1}${c+1}${d+1} ${mod[a]} ${mod[b]} ${mod[c]} ${mod[d]})")
    }
    /*

    "bad-sort".test {
        comment("pure modifier not first ")
        mm0("strict pure sort s3;")
        mmu("(sort s3 strict pure sort)")
    }

    "bad-sort".test {
        comment("pure modifier not first ")
        mm0("provable pure sort s5;")
        mmu("(sort s5 provable pure sort)")
    }

    "bad-sort".test {
        comment("pure modifier not first ")
        mm0("free pure sort s9;")
        mmu("(sort s9 free pure sort)")
    }

    "bad-sort".test {
        comment("free modifier not last ")
        mm0("free strict sort s10;")
        mmu("(sort s10 free strict sort)")
    }

    "bad-sort".test {
        comment("free modifier not last ")
        mm0("free provable sort s12;")
        mmu("(sort s12 free provable sort)")
    }

    "bad-sort".test {
        comment("provable modifier before strict")
        mm0("provable strict sort s12;")
        mmu("(sort s12 provable strict sort)")
    }
*/

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