package net.codenest.kassemblix.parser

/**
 * A KCharTerminal is a terminal that matches characters from a character assembly.
 * This is the base class for character-based terminals like KChar, KLetter, KDigit, etc.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
abstract class KCharTerminal(name: String = "", level: Int = 0, private var discard: Boolean = false) : KParser<Char>(name, level) {

    /**
     * By default, create a new assembly like the old one and advance it by one element.
     */
    override fun match(assemblies: List<KAssembly<Char>>): List<KAssembly<Char>> {
        val out = mutableListOf<KAssembly<Char>>()
        assemblies.filter { it.hasMoreItem() && qualify(it.peekItem()!!) }
            .forEach {
                val clone = it.clone()
                val next = clone.nextItem()!!
                if (!discard) clone.push(next)
                out.add(clone)
            }
        return out
    }

    /**
     * Returns true if the given character qualifies as the type of character this terminal looks for.
     *
     * @param c the character to check
     * @return true if the character qualifies
     */
    abstract fun qualify(c: Char): Boolean

    /**
     * Sets this terminal to discard matched characters.
     *
     * @return this terminal for chaining
     */
    fun discard(): KCharTerminal {
        discard = true
        return this
    }
}
