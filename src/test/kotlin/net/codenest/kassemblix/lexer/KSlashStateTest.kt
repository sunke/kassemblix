package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KToken.Companion.createSymbol
import org.junit.jupiter.api.Test

class KSlashStateTest: KTokenStateTest(state = KSlashState) {

    @Test
    fun testSlashStar() {
        assertToken(KToken.SKIP, "/*xxx*/")
        assertToken(KToken.SKIP, "/**/")
        assertToken(KToken.SKIP, "/*xxx*/end", "end")
        assertToken(KToken.SKIP, "/*1\nx2*/end", "end")
    }

    @Test
    fun testSlashSlash() {
        assertToken(KToken.SKIP, "//bla")
        assertToken(KToken.SKIP, "//1\n2", "2")
    }

    @Test
    fun testInvalidSlash() {
        assertToken(createSymbol("/"), "/x", "x")
        assertThrow("/*/x", "Unmatched slash symbol: /*/x")
        assertThrow("/**x", "Unmatched slash symbol: /**x")
    }
}