package net.codenest.kassemblix.parser


const val ERR_AMBIGUOUS_GRAMMAR = "Detect ambiguity in the parsing"

/**
 * A parser is an object that recognizes a language.
 */
abstract class KParser<T>(private val name: String = "", private val level: Int = 0) {


    private var assembler: KAssembler<T>? = null

    fun setAssembler(assembler: KAssembler<T>): KParser<T> {
        this.assembler = assembler
        return this
    }

    /**
     * Return a new Assembly without unrecognized items or NULL.
     */
    fun completeMatch(assembly: KAssembly<T>): KAssembly<T>? {
        val best = bestMatch(assembly)
        return if (!best!!.hasMoreItem()) best else null
    }

    /**
     * Return a new Assembly with the least unrecognized items.
     */
    fun bestMatch(assembly: KAssembly<T>): KAssembly<T>? {
        val ays = matchAndAssemble(listOf(assembly)).sortedBy { it.remainItemNr() }

        // detect ambiguity
        if (ays.size >= 2 && ays[0].remainItemNr() == 0 && ays[1].remainItemNr() == 0) {
            throw Exception(ERR_AMBIGUOUS_GRAMMAR)
        }

        return ays.getOrNull(0)
    }

    /**
     * Match the given assemblies and applying assemblers to them.
     */
    fun matchAndAssemble(assemblies: List<KAssembly<T>>): List<KAssembly<T>> {

        if (assemblies.isEmpty()) return assemblies

        return match(assemblies).onEach { assembler?.workOn(it) }
    }

    /**
     * Given a list of assemblies, this method matches this parser against all of them,
     * and returns a new list of the assemblies that result from the matches.
     *
     * For example, consider matching the regular expression <code>a*</code> against
     * the string <code>"aaab"</code>. The initial set of states is <code>{^aaab}</code>,
     * where the ^ indicates how far along the assembly is. When <code>a*</code> matches
     * against this initial state, it creates a new set <code>{^aaab, a^aab, aa^ab,
     * aaa^b}</code>.
     *
     * @param assemblies a list of assemblies to match against
     *
     * @return a list of assemblies that result from matching against the given assemblies
     */
    abstract fun match(assemblies: List<KAssembly<T>>): List<KAssembly<T>>
}