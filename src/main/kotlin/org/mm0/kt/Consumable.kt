package org.mm0.kt

interface Consumable {


    fun clone(): Consumable

    /** returns true if all chars have been consumed */
    fun isConsumed(): Boolean

    /** reports the current char and don't move */
    fun look(): Char


    /** gobbles all whitespace chars (' ' and '\n') :
    -- line comments \n
     */
    fun consume()

    /** returns true if Gobbler has text and advances after text
     * returns false and do not move otherwise
     * */
    fun consumeIf(text: String): Boolean

    /** same with a single char */
    fun consumeIf(char: Char): Boolean

    /** consume an intString (like "237") and returns an int
     * or returns null */
    fun consumeInt(): Int?

    /** consumes and returns one id
     * returns null if it cannot (in which case it doesn't move)
     *
     * allowDummy = true if id may contain a dot
     *            = false otherwise
     * */
    fun consumeId(allowDummy: Boolean): CharSequence?

    /** consumes and returns an immutable CharSequence
     * starting with ( and ending with balanced )
     * or returns null without moving */
    fun consumeBalancedParenthesis(): CharSequence?

    /** consumes a formula starting with $ and ending with $
     * and returns an untrimmed String for the inside
     * or returns null without moving */
    fun consumeFormula(): String?

    /** consumes everything till the next line 'EOF or '\n' */
    fun consumeLine():String

    /** reports current position */
    fun position(): Int

    /** returns charSequences with bounds=Pair(start, limit) */
    fun charSequences(bounds: List<Pair<Int, Int>>): List<CharSequence>

    companion object {
        const val INT_OVERFLOW_WITH: Int = (Int.MAX_VALUE - 9) / 10
    }
}