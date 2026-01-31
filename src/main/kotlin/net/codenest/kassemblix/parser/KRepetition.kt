package net.codenest.kassemblix.parser

class KRepetition<T>(name: String = "", level: Int = 0, private val subParser: KParser<T>) : KParser<T>("Repetition $name", level) {

    override fun match(assemblies: List<KAssembly<T>>): List<KAssembly<T>> {
        val out = assemblies.toMutableList()

        var ays = assemblies
        while (ays.isNotEmpty()) {
            ays = subParser.matchAndAssemble(ays)
            out.addAll(ays)
        }
        return out
    }
}