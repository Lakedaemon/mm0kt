package org.mm0.kt


@ExperimentalUnsignedTypes
class CommentsBuilder {
    val stream = mutableListOf<UByte>()
    val comments = mutableListOf<String>()
    var byte = 0u
    var k = 0

    fun skip() = append(0u)

    fun add(isNewline: Boolean) = append(if (isNewline) 1u else 2u)

    fun add(comment: String) {
        comments.add(comment)
        append(3u)
    }

    fun get(): Comments {
        if (k > 0) stream.add(byte.toUByte())
        val ret = Comments(comments.toList(), stream.toUByteArray())
        stream.clear()
        comments.clear()
        k = 0
        byte = 0u
        return ret
    }

    private fun append(value:UInt) {
        byte = byte.shl(2) or value
        if (++k == 4) {
            stream.add(byte.toUByte())
            k = 0
            byte = 0u
        }
    }
}