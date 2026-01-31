package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.lexer.KTokenType

class KSymbol(name: String = "Symbol", level: Int = 0, var symbol: KToken): KTerminal(name, level) {

    constructor(ch: Char, level: Int = 0): this(ch.toString(), level)

    constructor(str: String, level: Int = 0):
            this(name = "Symbol: $str", level = level, symbol = KToken(KTokenType.TT_SYMBOL, str, 0.0))

    override fun qualify(token: KToken) = symbol == token
}