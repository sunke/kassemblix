package net.codenest.kassemblix.parser

/**
 * A KChar matches any character from a character assembly.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KChar : KCharTerminal("Char") {

    /**
     * Returns true every time, since this terminal matches any character.
     *
     * @param c the character (ignored)
     * @return true, always
     */
    override fun qualify(c: Char): Boolean = true
}
