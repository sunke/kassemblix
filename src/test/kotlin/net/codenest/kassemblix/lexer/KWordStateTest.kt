package net.codenest.kassemblix.lexer

import net.codenest.kassemblix.lexer.KToken.Companion.createWord
import org.junit.jupiter.api.Test

class KWordStateTest : KTokenStateTest(state = KWordState)  {

    @Test
    fun testWord() {
        (state as KWordState).setBlankAllowed(false)
        assertToken(createWord("hello"), "hello world", " world")
        assertToken(createWord("Peter's"), "Peter's book", " book")
        assertToken(createWord("Jan-Willem"), "Jan-Willem Schut", " Schut")
        assertToken(createWord("test_1"), "test_1, test_2", ", test_2")
    }

    @Test
    fun testWordWithWhitespace() {
        (state as KWordState).setBlankAllowed(true)
        assertToken(createWord("Toasty Rita"), "Toasty Rita, Italian", ", Italian")
    }
}