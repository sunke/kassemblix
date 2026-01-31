package net.codenest.kassemblix.lexer

import org.junit.jupiter.api.Assertions.assertEquals
import java.io.PushbackReader
import java.io.StringReader
import kotlin.test.assertFailsWith

abstract class KTokenStateTest(var state: KTokenizerState) {

    fun assertToken(expect: KToken, str: String, expectRest: String = "") {
        val reader = PushbackReader(StringReader(str), 10)
        assertEquals(expect, state.nextToken(reader.read().toChar(), reader))
        assertEquals(expectRest, readRest(reader))
    }

    fun assertThrow(str: String, exceptionMessage: String) {
        val exception = assertFailsWith<Exception> {
            val reader = PushbackReader(StringReader(str))
            state.nextToken(reader.read().toChar(), reader)
        }
        assertEquals(exceptionMessage, exception.message)
    }

    private fun readRest(reader: PushbackReader): String {
        var rest = ""
        var ch = reader.read()
        while (ch != -1) {
            rest += ch.toChar().toString()
            ch = reader.read()
        }
        return rest
    }
}
