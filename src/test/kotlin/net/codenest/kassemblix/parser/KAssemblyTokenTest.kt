package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.lexer.KToken.Companion.createNumber
import net.codenest.kassemblix.lexer.KToken.Companion.createQuote
import net.codenest.kassemblix.lexer.KToken.Companion.createSymbol
import net.codenest.kassemblix.lexer.KToken.Companion.createWord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KAssemblyTokenTest {

    @Test
    fun `test token assembly`() {
        val expect = listOf(createWord("Let's"), createQuote("'rock and roll'"), createSymbol("!"))
        assertTokens(expect, "Let's 'rock and roll'!")
    }

    @Test
    fun `test no delimiters`() {
        val expect = listOf(createWord("NoDelimitersHere"))
        assertTokens(expect, "NoDelimitersHere")
    }

    @Test
    fun `test special characters`() {
        val expect = listOf(createSymbol("@"), createSymbol("#"), createSymbol("$"))
        assertTokens(expect, "@#$")
    }

    @Test
    fun `test whitespace handling`() {
        val expect = listOf(createWord("leading"), createWord("and"), createWord("trailing"))
        assertTokens(expect, "  leading and trailing  ")
    }

    @Test
    fun `test mixed content`() {
        val expect = listOf(createWord("Mix"), createSymbol("!"), createQuote("'of'"), createNumber(2.0), createWord("content"))
        assertTokens(expect, "Mix!'of'2content")
    }

    @Test
    fun `test print assembly`() {
        assertEquals("[]|^Congress/admitted/Colorado/in/1876.0/.",
                KTokenAssembly("Congress admitted Colorado in 1876.", "/").toString())

        assertEquals("[]|^admitted/(/colorado/,/1876.0/)",
                KTokenAssembly("admitted(colorado, 1876)", "/").toString())
    }

    private fun assertTokens(expect: List<KToken>, str: String) {
        val assembly = KTokenAssembly(str)
        val actual = mutableListOf<KToken>()
        while(assembly.hasMoreItem()) {
            actual.add(assembly.nextItem()!!)
        }

        assertEquals(expect, actual)
    }
}