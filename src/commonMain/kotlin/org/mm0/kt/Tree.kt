package org.mm0.kt

interface Tree {
    val id: CharSequence
    val children: List<Tree>
}