package net.codenest.kassemblix.lexer

import java.io.PushbackReader


object KSlashState : KTokenizerState {

    override fun nextToken(ch: Char, reader: PushbackReader): KToken {
        require(ch == '/')

        var next = reader.read()
        if (next == -1) return KSymbolState.nextToken('/', reader)

        // '/*' state ignores everything up to a closing '*/', and then returns the tokenizer's next token.

        if (next == '*'.code) {
            var prev = 0
            val str = StringBuilder("/*")
            next = reader.read()
            while (next != -1) {
                str.append(next.toChar())
                if (prev == '*'.code && next == '/'.code) {
                    break;
                }
                prev = next
                next = reader.read()
            }
            if (next == -1) throw Exception("Unmatched slash symbol: $str")
            return KToken.SKIP
        }

        //  '//' state ignores everything up to an end-of-line and returns the tokenizer's next token.
        if (next == '/'.code) {
            next = reader.read()
            while (next != '\n'.code && next != '\r'.code && next != -1) {
                next = reader.read()
            }
            return KToken.SKIP
        }

        reader.unread(next)
        return KSymbolState.nextToken('/', reader)
    }
}