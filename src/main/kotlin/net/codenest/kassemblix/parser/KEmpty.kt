package net.codenest.kassemblix.parser

/**
 * A KEmpty parser matches any assembly once, and applies its assembler that one time.
 *
 * Language elements often contain empty parts. For example, a language may at some point
 * allow a list of parameters in parentheses, and may allow an empty list. An empty
 * parser makes it easy to match, within the parenthesis, either a list of parameters or "empty".
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
class KEmpty<T> : KParser<T>("Empty") {

    /**
     * Given a list of assemblies, this method returns clones of all assemblies as a successful match.
     *
     * @param assemblies the input assemblies
     * @return clones of all input assemblies
     */
    override fun match(assemblies: List<KAssembly<T>>): List<KAssembly<T>> {
        return assemblies.map { it.clone() }
    }
}
