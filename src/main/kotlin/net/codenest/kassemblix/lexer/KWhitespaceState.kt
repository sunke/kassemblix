package net.codenest.kassemblix.lexer

import java.io.PushbackReader


object KWhitespaceState : KTokenizerState {

    override fun nextToken(ch: Char, reader: PushbackReader): KToken {
        require(isWhitespace(ch))

        var next = reader.read()
        while (next != -1 && isWhitespace(next.toChar())) {
            next = reader.read()
        }

        if (next != -1) {
            reader.unread(next)
        }
        return KToken.SKIP
    }

    private fun isWhitespace(ch: Char) = KTokenizerStateTable.isWhitespace(ch)
}