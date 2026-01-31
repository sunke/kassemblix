package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken


abstract class KTerminal(name: String, level: Int = 0, private var discard: Boolean = false) : KParser<KToken>(name, level) {

    fun discard(): KTerminal {
        discard = true
        return this
    }

    /**
     * Given a list of assemblies, this method matches this terminal against all of them, and returns a new
     * list of the assemblies that result from the matches.
     */
    override fun match(assemblies: List<KAssembly<KToken>>): List<KAssembly<KToken>> {
        val out = mutableListOf<KAssembly<KToken>>()
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
     * Check that the given token qualifies as the type of terminal this terminal looks for.
     */
    abstract fun qualify(token: KToken): Boolean
}