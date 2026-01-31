package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken

class KNum(level: Int = 0) : KTerminal("Num", level) {

    override fun qualify(token: KToken) = token.isNumber()
}