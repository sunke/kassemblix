package net.codenest.kassemblix.parser

/**
 * A character assembly whose elements are characters from a string.
 */
class KCharAssembly(input: String = "") : KAssembly<Char>("") {
    init {
        input.forEach { addItem(it) }
    }
}