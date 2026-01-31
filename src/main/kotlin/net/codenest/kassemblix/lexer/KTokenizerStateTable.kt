package net.codenest.kassemblix.lexer

object KTokenizerStateTable {

    private val states = Array<KTokenizerState>(256) { KSymbolState }

    init {
        for (i in 0..' '.code) states[i] = KWhitespaceState
        for (i in 'a'.code..'z'.code) states[i] = KWordState
        for (i in 'A'.code..'Z'.code) states[i] = KWordState
        for (i in 0xc0..0xff) states[i] = KWordState
        for (i in '0'.code..'9'.code) states[i] = KNumberState
        states['-'.code] = KNumberState
        states['"'.code] = KQuoteState
        states['\''.code] = KQuoteState
        states['/'.code] = KSlashState
    }

    fun isValidChar(c: Int) = c in states.indices

    fun isWhitespace(ch: Char) = getState(ch, null) is KWhitespaceState

    fun isQuote(ch: Char) = getState(ch, null) is KQuoteState

    fun setState(ch: Char, state: KTokenizerState) {
        states[ch.code] = state
    }

    fun getState(ch: Char, previous: KTokenizerState?): KTokenizerState {
        if (ch == '-' && previous is KNumberState) {
            return KSymbolState
        }
        return states[ch.code]
    }
}