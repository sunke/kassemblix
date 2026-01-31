package net.codenest.kassemblix.lexer

import org.junit.jupiter.api.Test

class KWhitespaceStateTest : KTokenStateTest(state = KWhitespaceState)  {

    @Test
    fun testWhitespace() {
        assertToken(KToken.SKIP, "  ")
        assertToken(KToken.SKIP, "  x", "x")
        assertToken(KToken.SKIP, "\tx", "x")
    }
}