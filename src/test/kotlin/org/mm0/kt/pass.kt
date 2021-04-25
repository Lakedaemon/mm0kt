package org.mm0.kt


fun parsingPassForMM0() = passMM0("parsingMM0") {
    "math-string".test {
        comment("math-string ::= '\$' [^\\\$]* '\$'")
        both()
        both("""NUL SOH STX ETX EOT ENQ ACK BEL BS HT LF VT FF CR SO SI
        | DLE DC1 DC2 DC3 DC4 NAK SYN ETB CAN EM SUB ESC FS GS RS US
        |    ! " # % & ' ( ) * + , - . /
        |  0 1 2 3 4 5 6 7 8 9 : ; < = > ?
        |  @ A B C D E F G H I J K L M N O
        |  P Q R S T U V W X Y Z [ \ ] ^ _
        |  ` a b c d e f g h i j k l m n o
        |   p q r s t u v w x y z { | } ~ DEL""".trimMargin())
    }

    // int
    "int".test {
        comment("int number ::= 0 | [1-9][0-9]*", "Verifiers should support precedences up to at least 2^11 - 2 = 2046")
        op("a", precedence = 0, constant = "+")
        op("b", precedence = 2046, constant = "*")
        op("c", precedence = 1875, constant = "/")
        op("d", precedence = 39, constant = "-")
    }
}

fun parsingPassForMMU() = passMMU("parsingMMU") {

}

fun parsingPassForBoth() = passBoth("parsingBoth") {
    "comment".test {
        raw(" --  <-- comment doesn't start the line\n")
        mm0("sort \n-- comment inside a statement\ns;")
        mmu("(sort \n-- comment inside a directive\ns)")
    }

    // shipped
    "id".test {
        comment("identifier ::= [a-zA-Z_][a-zA-Z0-9_]*", mmu = listOf("identifier ::= [a-zA-Z_][a-zA-Z0-9_]*"))
        sort("id_1")
        sort("Id2_")
        sort("_id3")
    }
    // mm0 and mmu syntax

    // whitespace
    "whitespace".test {
        comment("    v-- this is a SPACE", mmu = listOf("     v-- this is a SPACE"))
        sort("s ")
    }
    "whitespace".test {
        comment("    v-- this is a NEWLINE", mmu = listOf("     v-- this is a NEWLINE"))
        sort("s\n")
    }


    // delimiters


    "duplicate delimiters".test { both("(", "(") }
    "duplicate delimiters".test { leftRight("(", "(") }
    "duplicate delimiters".test { leftRight(right = listOf("(", "(")) }
}

fun matchingPass() = passBoth("matching") {
    "sort".test {
        sort("s0", isPure = false, isStrict = false, isProvable = false, isFree = false)
        sort("s1", isPure = false, isStrict = false, isProvable = false, isFree = true)
        sort("s2", isPure = false, isStrict = false, isProvable = true, isFree = false)
        sort("s3", isPure = false, isStrict = false, isProvable = true, isFree = true)
        sort("s4", isPure = false, isStrict = true, isProvable = false, isFree = false)
        sort("s5", isPure = false, isStrict = true, isProvable = false, isFree = true)
        sort("s6", isPure = false, isStrict = true, isProvable = true, isFree = false)
        sort("s7", isPure = false, isStrict = true, isProvable = true, isFree = true)
        sort("s8", isPure = true, isStrict = false, isProvable = false, isFree = false)
        sort("s9", isPure = true, isStrict = false, isProvable = false, isFree = true)
        sort("s10", isPure = true, isStrict = false, isProvable = true, isFree = false)
        sort("s11", isPure = true, isStrict = false, isProvable = true, isFree = true)
        sort("s12", isPure = true, isStrict = true, isProvable = false, isFree = false)
        sort("s13", isPure = true, isStrict = true, isProvable = false, isFree = true)
        sort("s14", isPure = true, isStrict = true, isProvable = true, isFree = false)
        sort("s15", isPure = true, isStrict = true, isProvable = true, isFree = true)
    }

    // term
    "same order for binders".test {
        sort("s")
        term("a", "s", "(x:s)", "(y:s)")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        term("a", "s", "(x:s)", "(y:t)")
    }
    "same order for binders".test {
        sort("s")
        term("a", "s", "(x y:s)")
    }
    "same order for binders".test {
        sort("s")
        sort("t")
        term("a", "s > t > s")
    }
}

fun registeringPass() = passBoth("registering") {
    "reflexive coercion".test {
        sort("s")
        coercion("c", "s", "s")
    }
}

fun dynamicParsingPass() = passBoth("dynamic parsing") {}

fun proofCheckingPass() = passBoth("proofChecking") {
    "blank file".test { raw(" \n ") }
    "trivial file".test { both("( )") }
}

fun optionalBoth() = passBoth("optionalBoth") {
    // verifiers are only required to support up to prec 2046
    "big int support".test { op("plus", precedence = Int.MAX_VALUE - 1, constant = "+") }

    // Not a fail test, since verifiers are permitted to alpha rename. Maybe "optional success"?
    "different binders order".test {
        sort("s")
        mm0("term a (x y: s):s;")
        mmu("(term a ((y s ())(x s ())) (s ())")
    }
}

fun pass() {
    parsingPassForMM0()
    parsingPassForMMU()
    parsingPassForBoth()
    matchingPass()
    registeringPass()
    dynamicParsingPass()
    proofCheckingPass()
}