package org.mm0.kt

class StringConsumable(private val string: String, private var pos: Int = 0, private val lim: Int = string.length) : Consumable {
    override fun clone(): StringConsumable = StringConsumable(string, pos, lim)
    override fun isConsumed(): Boolean = pos >= lim
    override fun look(): Char = string[pos]
    override fun consume() {
        while (pos < lim) {
            val c = look()
            when {
                c == '-' -> if (!string.startsWith("--", pos)) return else {
                    pos += 2
                    while (pos < lim && look() != '\n') pos++
                }
                !c.isWhitespace() && c != '\n' -> return
            }
            pos++
        }
    }

    override fun consumeInt(): Int? {
        var int = 0
        var p = pos
        while (p < lim) {
            val c = string[p] - '0'
            if (c !in 0..9) break
            if (int > (Int.MAX_VALUE-c) / 10) return null
            int = int * 10 + c
            p++
        }
        if (p == pos) return null
        pos = p
        return int
    }


    override fun consumeIf(text: String): Boolean {
        if (!string.startsWith(text, pos)) return false
        pos += text.length
        return true
    }

    override fun consumeIf(char: Char): Boolean {
        if (pos >= lim || string[pos] != char) return false
        pos++
        return true
    }

    override fun consumeId(allowDummy: Boolean): CharSequence? {
        var position = pos
        var isFirstChar = true
        while (position < lim) {
            val c = string[position]
            val isDigit = c in '0'..'9'
            if (c !in 'a'..'z' && c !in 'A'..'Z' && !isDigit && c != '_' && (c != '.' || !allowDummy)) break
            if (isFirstChar && isDigit) return null
            isFirstChar = false
            position++
        }
        if (position == pos) return null
        val res = string.substring(pos, position)
        pos = position
        return res
    }

    override fun consumeBalancedParenthesis(): CharSequence? {
        val start = pos
        if (string[start] != '(') return null
        var outer = 1
        var a = start + 1
        while (a < lim) {
            when (string[a]) {
                '(' -> outer++
                ')' -> if (--outer == 0) {
                    pos = ++a
                    return string.substring(start, pos)
                }
            }
            a++
        }
        return null
    }

    override fun consumeFormula(): String? {
        if (look() != '$') return null
        val newPos = string.indexOf('$', pos + 1) + 1
        if (newPos <= 0) return null
        val start = pos + 1
        pos = newPos
        return string.substring(start, newPos - 1)
    }

    override fun position(): Int = pos
    override fun charSequences(bounds: List<Pair<Int, Int>>): List<CharSequence> = bounds.map { string.substring(it.first, it.second.coerceAtMost(lim)) }
}