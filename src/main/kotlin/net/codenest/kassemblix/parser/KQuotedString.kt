package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken

/**
 * A KQuotedString matches a quoted string, like "this one", from a token assembly.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KQuotedString : KTerminal("QuotedString") {

    /**
     * Returns true if the token is a quoted string.
     *
     * @param token the token to check
     * @return true if the token is a quoted string
     */
    override fun qualify(token: KToken): Boolean = token.isQuotedString()
}
