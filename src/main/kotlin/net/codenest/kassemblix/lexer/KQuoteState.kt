package net.codenest.kassemblix.lexer

import java.io.PushbackReader


/**
 * A QuoteState object returns a quoted string from a reader. This object will collect characters until it sees a match
 * to the character that the tokenizer used to switch to this state.
 */
object KQuoteState : KTokenizerState {

    override fun nextToken(ch: Char, reader: PushbackReader): KToken {
        require(KTokenizerStateTable.isQuote(ch))

        val str = StringBuilder(ch.toString())
        var next: Int
        do {
            next = reader.read()
            if (next != -1) { str.append(next.toChar()) }
        } while(next != ch.code && next != -1)

        // not matched quote until the end.
        if (next == -1) {
            throw Exception("Unmatched quote symbol: $str")
        }

        return KToken(KTokenType.TT_QUOTED, str.toString(), 0.0)
    }
}