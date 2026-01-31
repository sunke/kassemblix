package net.codenest.kassemblix.example.arithmetic

import net.codenest.kassemblix.lexer.KToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KArithmeticParserTest {
    @Test
    fun testAssociativity() {
        assertEquals(4.0, calculate("7 - 3"))
        assertEquals(4.0, calculate("7-3"))
        assertEquals(3.0, calculate("7 - 3 - 1"))
        assertEquals(2.0, calculate("2 ^ 1 ^ 4"))
    }

    @Test
    fun testIllegalExpression() {
        assertImproperlyFormed("7 / ")
        assertImproperlyFormed("7 ++ 6 ")
        assertImproperlyFormed("7 **/ 2 ")
        assertImproperlyFormed("7^*2")
    }

    @Test
    fun testParentheses() {
        assertEquals(18.0, calculate("((3 * 7) + (11 * 3)) / 3"))
    }

    @Test
    fun testPrecedence() {
        assertEquals(7.0, calculate("7 - 3 * 2 + 6"))
        assertEquals(2.0, calculate("2^1^4"))
        assertEquals(512.0, calculate("2^3^2"))
        assertEquals(1512.0, calculate("1000+2*2^3^2/2"))
        assertEquals(36.0, calculate("3*2^2*3"))
    }

    private fun checkValue(expected: Double, actual: Double) {
        assertEquals(expected, actual, KToken.NUMBER_DELTA_TOLERANCE);
    }

    private fun assertImproperlyFormed(exp: String) {
        val exception = assertThrows(Exception::class.java) { calculate(exp) }
        assertEquals(ERR_IMPROPERLY_FORMED, exception.message)
    }
}