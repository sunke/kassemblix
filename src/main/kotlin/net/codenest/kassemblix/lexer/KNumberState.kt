package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KToken.Companion.createNumber
import java.io.PushbackReader
import kotlin.math.pow

/**
 * A NumberState object returns a number from a reader. This state's idea of a number allows an optional, initial
 * minus sign, followed by one or more digits. A decimal point and another string of digits may follow these digits.
 */
object KNumberState: KTokenizerState {

    override fun nextToken(ch: Char, reader: PushbackReader): KToken {
        require(isDigit(ch.code) || ch == '-')

        var isNegative = false
        var next = ch.code
        var value = 0.0

        // get negative sign
        if (next == '-'.code) {
            isNegative = true
            next = reader.read()
            if (next == -1 || !isDigit(next)) {
                if (next != -1) reader.unread(next)
                return KSymbolState.nextToken('-', reader)
            }
        }
        if (!isDigit(next)) return KSymbolState.nextToken(next.toChar(), reader)

        // get integer part
        var endWithComma = false
        while (isDigit(next) || isComma(next)) {
            endWithComma = isComma(next)
            if (isDigit(next)) value = value * 10 + (next - '0'.code)
            next = reader.read()
            if (next == -1) return getNumber(isNegative, value)
        }
        if (endWithComma) {
            reader.unread(next)
            reader.unread(','.code)
            return getNumber(isNegative, value)
        }

        // get fraction part
        if (next == '.'.code) {
            next = reader.read()
            if (next == -1 || !isDigit(next)) {
                if (next != -1) reader.unread(next)
                reader.unread('.'.code)
                return getNumber(isNegative, value)
            }

            var place = 0.1
            while (isDigit(next)) {
                value += (next - '0'.code) * place
                place *= 0.1
                next = reader.read()
            }
        }

        // check scientific notation
        if (isExp(next)) {
            value *= 10.0.pow(getExpValue(next, reader))
            return getNumber(isNegative, value)
        }

        if (next != -1) reader.unread(next)
        return getNumber(isNegative, value)
    }

    private fun getExpValue(exp: Int, reader: PushbackReader): Double {
        var isNegative = false
        var value = 0.0

        var next = reader.read()
        if (next == -1) {
            reader.unread(exp)
            return 0.0
        }

        // read negative sign
        if (next == '-'.code) {
            isNegative = true
            next = reader.read()
            if (next == -1) {
                reader.unread('-'.code)
                reader.unread(exp)
                return 0.0
            }
        }

        // the first letter after exp or '-' is not number
        if (!isDigit(next)) {
            reader.unread(next)
            if (isNegative) reader.unread('-'.code)
            reader.unread(exp)
            return 0.0
        }

        // read exp value
        while (isDigit(next)) {
            value = value * 10 + (next - '0'.code)
            next = reader.read()
        }

        return if (isNegative) -value else value
    }

    private fun getNumber(isNegative: Boolean, value: Double) = createNumber(if (isNegative) -value else value)

    private fun isDigit(c: Int) = c in '0'.code..'9'.code

    private fun isComma(c: Int) = c == ','.code

    private fun isExp(c: Int) = c == 'e'.code || c == 'E'.code
}