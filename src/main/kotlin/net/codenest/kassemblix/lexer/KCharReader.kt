package net.codenest.kassemblix.lexer

import java.io.Reader

/**
 * A character reader that allows characters to be pushed back to the stream.
 */
class KCharReader(private val input: Reader) {

    private var buf = mutableListOf<Int>()

    var row = 1


    fun read(): Int {
        val c = if (buf.isNotEmpty()) buf.removeAt(buf.size - 1) else input.read()
        if (c == '\n'.code) row++
        return c
    }

    fun unread(c: Int) {
        if (c != -1) unread(c.toChar())
    }

    fun unread(ch: Char) {
        buf.add(ch.code)
        if (ch == '\n') row--
    }

    fun close() {
        input.close()
    }

    private fun printChar(c: Int) =
        when (c) {
            -1 -> "-1"
            '\n'.code -> "\\n"
            '\r'.code -> "\\r"
            else -> c.toChar().toString()
        }
}