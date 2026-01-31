package net.codenest.kassemblix.parser

/**
 * A KDigit matches any digit from a character assembly.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KDigit : KCharTerminal("Digit") {

    /**
     * Returns true if the character is a digit.
     *
     * @param c the character to check
     * @return true if the character is a digit (0-9)
     */
    override fun qualify(c: Char): Boolean = c.isDigit()
}
