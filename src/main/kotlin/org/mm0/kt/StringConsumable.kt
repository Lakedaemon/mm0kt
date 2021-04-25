package org.mm0.kt

class StringConsumable(private val string: String, private var pos: Int = 0, private val lim: Int = string.length, private val recordComments:Boolean=false) : Consumable {
    @ExperimentalUnsignedTypes
    private val builder = CommentsBuilder()
    override fun clone(): StringConsumable = StringConsumable(string, pos, lim)
    override fun isConsumed(): Boolean = pos >= lim
    override fun look(): Char = string[pos]
    @ExperimentalUnsignedTypes
    override fun consume() {
        if (recordComments) builder.skip()
        while (pos < lim) {
            val c = look()
            when {
                c == '-' -> if (!string.startsWith("--", pos)) return else {
                    pos += 2
                    val start = pos
                    while (pos < lim && look() != '\n') pos++
                    if (recordComments) builder.add(string.substring(start, pos))
                }
                c == '\n' -> if (recordComments) builder.add(true)
                c == ' ' -> if (recordComments) builder.add(false)
                else -> return
            }
            pos++
        }
    }

    override fun consumeLine(): String {
        val p = pos
        while (pos < lim && look() != '\n') pos++
        return string.substring(p, pos++)
    }

    override fun consumeInt(): Int? {
        var int = 0
        var p = pos
        while (p < lim) {
            val c = string[p] - '0'
            if (c !in 0..9) break
            // guard against int overflowing
            if (int > (Int.MAX_VALUE - c) / 10) return null
            int = int * 10 + c
            p++
        }
        // int number ::= 0 | [1-9][0-9]*
        when {
            p == pos -> return null
            int == 0 && p != pos + 1 -> return null
            int > 0 && string[pos] == '0' -> return null
        }
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
            // an id cannot start with a digit
            if (isFirstChar && isDigit) return null
            isFirstChar = false
            position++
        }
        // empty id aren't allowed
        if (position == pos) return null
        // single _ cannot be an id
        if (position == pos + 1 && string[pos] == '_') return null
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

    @ExperimentalUnsignedTypes
    override fun comments(): Comments = builder.get()

    override fun position(): Int = pos
    override fun charSequences(bounds: List<Pair<Int, Int>>): List<CharSequence> = bounds.map { string.substring(it.first, it.second.coerceAtMost(lim)) }
}