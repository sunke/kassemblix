package net.codenest.kassemblix.lexer

import java.io.PushbackReader


object KWordState : KTokenizerState {
    private var allowBlank = false

    override fun nextToken(ch: Char, reader: PushbackReader): KToken {
        require(KTokenizerStateTable.getState(ch, null) is KWordState)

        var next = reader.read()
        val str = StringBuilder(ch.toString())
        while (next != -1 && isWordCharacter(next.toChar())) {
            str.append(next.toChar())
            next = reader.read()
        }
        if (next != -1) {
            reader.unread(next)
        }

        return KToken(KTokenType.TT_WORD, str.toString(), 0.0)
    }

    fun setBlankAllowed(b: Boolean) = apply { allowBlank = b }

    private fun isWordCharacter(ch: Char): Boolean {
        if (KTokenizerStateTable.getState(ch, null) is KWordState) return true
        if (ch == '\'' || ch == '-' || ch == '_') return true
        if (ch in '0'..'9') return true
        if (allowBlank && KTokenizerStateTable.getState(ch, null) is KWhitespaceState) return true

        return false
    }
}