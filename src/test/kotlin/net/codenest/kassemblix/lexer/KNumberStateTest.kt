package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KToken.Companion.createNumber
import net.codenest.kassemblix.lexer.KToken.Companion.createSymbol
import org.junit.jupiter.api.Test


internal class KNumberStateTest: KTokenStateTest(state = KNumberState) {

    @Test
    fun testNormalNumber() {
        assertToken(createNumber(12345.0), "12345")
        assertToken(createNumber(42.675), "42.675")
        assertToken(createNumber(-0.124), "-0.124")
        assertToken(createNumber( 0.0), "-0.0")
    }

    @Test
    fun testNumberComma() {
        assertToken(createNumber(12345.0), "12,345")
        assertToken(createNumber(4342.675), "4,342.675")
    }

    @Test
    fun testScientificNotation() {
        assertToken(createNumber(602.0), "6.02e2")
        assertToken(createNumber(0.016), "1.6E-2")

        assertToken(createNumber(-5.0), "-5e", "e")
        assertToken(createNumber(23.0), "23e-", "e-")
    }

    @Test
    fun testValidateNumber() {
//        assertToken(createNumber(4.0), "4.a", ".a")
//        assertToken(createNumber(12.0), "12 t", " t")
//        assertToken(createSymbol("-"), "-a", "a")
//        assertToken(createSymbol("-"), "-.123", ".123")
        assertToken(createSymbol("-"), "-")
//        assertToken(createNumber(6.0), "6,b", ",b")
//        assertToken(createNumber(1.23), "1.23,6b", ",6b")
//        assertToken(createSymbol("-"), "-,234", ",234")
    }
}