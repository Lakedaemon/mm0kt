package org.mm0.kt

class StringTree(override val id: String, override val children: List<StringTree>) : Tree {
    override fun toString() = StringBuilder().print(this, 0).toString()
    override fun equals(other: Any?): Boolean = other is StringTree && id == other.id && children == other.children
    override fun hashCode(): Int = id.hashCode() * 31 + children.hashCode()

    private fun StringBuilder.print(t: StringTree, level: Int): StringBuilder = apply {
        append("  ".repeat(level))
        append(t.id)
        append("\n")
        for (child in t.children) print(child, level + 1)
    }
}