package net.codenest.kassemblix.parser

class KSequence<T>(name: String = "", level: Int = 0) : KParser<T>("Sequence $name", level) {

    private val subParsers = mutableListOf<KParser<T>>()

    fun add(parser: KParser<T>) = apply { subParsers.add(parser) }

    override fun match(assemblies: List<KAssembly<T>>)
            = subParsers.fold(assemblies) { ays, p -> p.matchAndAssemble(ays) }
}
