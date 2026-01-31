package net.codenest.kassemblix.parser

class KAlternation<T>(name: String = "", level: Int = 0) : KParser<T>("Alternation $name", level) {

    private var subParsers = mutableListOf<KParser<T>>()

    fun add(parser: KParser<T>) = apply { subParsers.add(parser) }

    override fun match(assemblies: List<KAssembly<T>>): List<KAssembly<T>> {
        var out = mutableListOf<KAssembly<T>>()
        subParsers.forEach { out.addAll(it.matchAndAssemble(assemblies)) }
        return out
    }
}