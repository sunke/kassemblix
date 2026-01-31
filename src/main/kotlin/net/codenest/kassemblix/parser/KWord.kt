package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken

class KWord(level: Int = 0) : KTerminal("Word", level) {

    override fun qualify(token: KToken) = token.isWord()
}