package net.codenest.kassemblix.parser

/**
 * A KLetter matches any letter from a character assembly.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KLetter : KCharTerminal("Letter") {

    /**
     * Returns true if the character is a letter.
     *
     * @param c the character to check
     * @return true if the character is a letter
     */
    override fun qualify(c: Char): Boolean = c.isLetter()
}
