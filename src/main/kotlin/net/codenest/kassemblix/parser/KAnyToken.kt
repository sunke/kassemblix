package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken

/**
 * A KAnyToken matches any token from a token assembly.
 * This is the Kotlin equivalent of Java's Terminal class with default qualifies() behavior.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KAnyToken : KTerminal("Any") {

    /**
     * Returns true for any token, since this terminal matches any token type.
     *
     * @param token the token (ignored)
     * @return true, always
     */
    override fun qualify(token: KToken): Boolean = true
}
