package net.codenest.kassemblix.parser

/**
 * A KSpecificChar matches a specific character from a character assembly.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KSpecificChar(private val character: Char) : KCharTerminal("'$character'") {

    /**
     * Returns true if the character equals the specific character this terminal matches.
     *
     * @param c the character to check
     * @return true if the character matches
     */
    override fun qualify(c: Char): Boolean = c == character
}
