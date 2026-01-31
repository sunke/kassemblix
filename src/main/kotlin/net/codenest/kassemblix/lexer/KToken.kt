package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KTokenType.*
import kotlin.math.abs


/**
  * A token represents a logical chunk of a string. For example, a typical tokenizer would break the string
  * `"1.23 <= 12.3"` into three tokens: the number 1.23, a less-than-or-equal symbol, and the number 12.3.
  * A token is a receptacle, and relies on a tokenizer to decide precisely how to divide a string into tokens.
  */
class KToken (var ttype: KTokenType, var sval: String?, var nval: Double) {

    companion object {
        const val NUMBER_DELTA_TOLERANCE = 1e-6

        val START = KToken(TT_START, "", 0.0)
        val END = KToken(TT_END, "", 0.0)
        val SKIP = KToken(TT_SKIP, "", 0.0)

        fun createSymbol(str: String) = KToken(TT_SYMBOL, str, 0.0)
        fun createNumber(number: Double) = KToken(TT_NUMBER, "", number)
        fun createWord(word: String) = KToken(TT_WORD, word, 0.0)
        fun createQuote(str: String) = KToken(TT_QUOTED, str, 0.0)
    }

    fun isNumber() = ttype === TT_NUMBER

    fun isQuotedString() = ttype === TT_QUOTED

    fun isSymbol() = ttype === TT_SYMBOL

    fun isWord() = ttype === TT_WORD

    fun value(): Any = when(ttype) {
        TT_NUMBER -> nval
        else -> sval ?: ttype
    }

    fun equals(other: Any?, ignoreCase: Boolean = false): Boolean {
        if (other !is KToken) return false

        if (ttype !== other.ttype) return false

        return when(ttype) {
            TT_NUMBER -> abs(nval - other.nval) < NUMBER_DELTA_TOLERANCE
            else -> sval?.equals(other.sval, ignoreCase) ?: (other.sval === null)
        }
    }

    override fun equals(other: Any?) = equals(other, false)

    override fun hashCode(): Int {
        var result = ttype.hashCode()
        result = 31 * result + (sval?.hashCode() ?: 0)
        result = 31 * result + nval.hashCode()
        return result
    }

    override fun toString() = when {
        ttype === TT_START -> "START"
        ttype === TT_END -> "END"
        ttype === TT_SKIP -> "SKIP"
        else -> value().toString()
    }
}

enum class KTokenType {
    TT_SKIP,
    TT_START,
    TT_END,
    TT_NUMBER,
    TT_WORD,
    TT_SYMBOL,
    TT_QUOTED
}
