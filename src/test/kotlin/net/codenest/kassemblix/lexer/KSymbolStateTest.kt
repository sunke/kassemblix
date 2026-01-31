package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KToken.Companion.createSymbol
import org.junit.jupiter.api.Test

class KSymbolStateTest : KTokenStateTest(state = KSymbolState)  {

    @Test
    fun testSingleSymbol() {
        assertToken(createSymbol("="), "=2", "2")
        assertToken(createSymbol(">"), ">a", "a")
        assertToken(createSymbol("$"), "$12", "12")
        assertToken(createSymbol("#"), "#abc", "abc")
    }

    @Test
    fun testMultiCharSymbol() {
        assertToken(createSymbol("!="), "!=2", "2")
        assertToken(createSymbol(">="), ">=x", "x")
        assertToken(createSymbol("<="), "<=!", "!")
    }

    @Test
    fun testNewSymbol() {
        (state as KSymbolState).addSymbol("=~=")
        assertToken(createSymbol("=~="), "=~=42", "42")
    }
}