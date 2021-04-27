package org.mm0.kt

fun Consumable.skipIf(char: Char): Boolean {
    if (!consumeIf(char)) return false
    consume()
    return true
}

fun Consumable.skipIf(text: String): Boolean {
    if (!consumeIf(text)) return false
    consume()
    return true
}