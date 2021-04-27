package org.mm0.kt

class Comments @ExperimentalUnsignedTypes constructor(val comments: List<String>, val stream: UByteArray) : Iterable<String> {
    @ExperimentalUnsignedTypes
    override fun iterator(): Iterator<String> = object : Iterator<String> {
        var pos = 0
        var n = 0
        var k = 0
        override fun hasNext(): Boolean = n < stream.size && k < 4

        override fun next(): String {
            val res = when (stream[n].toUInt().shr(k.shl(1))) {
                0u -> ""
                1u -> " "
                2u -> "\n"
                else -> comments[pos++]
            }
            when {
                k < 3 -> ++k
                n < stream.size -> {
                    ++n
                    k = 0
                }
            }
            return res
        }
    }
}