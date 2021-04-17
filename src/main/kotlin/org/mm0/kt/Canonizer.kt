package org.mm0.kt

// TODO making this thread safe is harder than what it seems
// map access should be made thread safe :/
// not just Proxy...

/** a canonizer should only ever be used (sequentially) on a single thread (or performance would degrade because of concurrency)
 *
 * for this to work, SubSequence MUST have the same hashcode than strings
 * OR we would have to wrap keys in a SubSequence (acceptable compromise)
 * OR we could use a Tree instead of a HashMap (but performance would degrade probably)
 * */
class Canonizer {
    private val charSequenceCanonizer = mutableMapOf<CharSequence, String>().apply { this[underscoreCS] = underscoreCS }

    private class SubSequence(var cs: CharSequence, var pos: Int, var lim: Int) : CharSequence {
        override val length: Int get() = lim - pos
        override fun get(index: Int): Char = cs[pos + index]
        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = SubSequence(cs, pos + startIndex, pos + endIndex)
        override fun equals(other: Any?): Boolean = other is CharSequence && charsEquals(other)
        override fun hashCode(): Int = charsHashCode()
        override fun toString(): String = cs.substring(pos, lim)
    }

    private val sub = SubSequence("", 0, 0)

    fun toImmutable(charSequence: CharSequence, position: Int = 0, limit: Int = charSequence.length): String {
        val string = charSequenceCanonizer[sub.apply { cs = charSequence;pos = position;lim = limit }]
        if (string != null) return string
        val result = sub.toString()
        charSequenceCanonizer[result] = result
        return result
    }

    /*fun toImmutable(slice: CharSequence): String {
        val immutable = charSequenceCanonizer[slice]
        if (immutable != null) return immutable
        val result = slice.toString()
        charSequenceCanonizer[result] = result
        return result
    }*/


    private val typeCanonizer = mutableMapOf<ProxyCharSequenceCharSequences, Type>()

    /** thread safe, through the use of ThreadLocal */
    fun toImmutable(sort: CharSequence, dependencies: Iterable<CharSequence>): Type {
        val result = typeCanonizer[threadLocalProxy.get().apply { charSequence = sort;iterable = dependencies }]
        if (result != null) return result
        val type = Type(sort = toImmutable(sort), dependencies = dependencies.asSequence().map { toImmutable(it) }.toList())
        typeCanonizer[ProxyCharSequenceCharSequences(type.sort, type.dependencies)] = type
        return type
    }

    private val threadLocalProxy: ThreadLocal<ProxyCharSequenceCharSequences> = ThreadLocal<ProxyCharSequenceCharSequences>().apply { set(ProxyCharSequenceCharSequences("", listOf())) }

    /*private class ProxyCharSequences(var iterable: Iterable<CharSequence>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ProxyCharSequences) return false
            val itA = iterable.iterator()
            val itB = other.iterable.iterator()
            while (itA.hasNext() && itB.hasNext()) if (itA.next() != itB.next()) return false
            return !itA.hasNext() && !itB.hasNext()
        }

        override fun hashCode(): Int {
            var int = 0
            for (charSequence in iterable) int = int * 31 + charSequence.hashCode()
            return int
        }
    }*/

    private class ProxyCharSequenceCharSequences(var charSequence: CharSequence, var iterable: Iterable<CharSequence>) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ProxyCharSequenceCharSequences) return false
            if (!charSequence.charsEquals(other.charSequence)) return false
            val itA = iterable.iterator()
            val itB = other.iterable.iterator()
            while (itA.hasNext() && itB.hasNext()) if (itA.next() != itB.next()) return false
            return !itA.hasNext() && !itB.hasNext()
        }

        override fun hashCode(): Int {
            var int = charSequence.hashCode()
            for (charSequence in iterable) int = int * 31 + charSequence.hashCode()
            return int
        }
    }


    // TODO implement canonization fro binders
    //  this would save the storage for the list of references
    fun toImmutable(binders: Iterable<Binder>) = binders.toList()

    // TODO implement canonization for MathTree

    /*companion object {
        private val noCanonizer = fun CharSequence.(): String = toString()
    }*/
}