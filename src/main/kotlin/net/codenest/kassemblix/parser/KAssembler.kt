package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken


/**
 * An assembler helps a parser build a result.
 *
 * Parsers that have an Assembler ask it to work on an assembly after a successful match.
 * By default, terminals push their matches on an assembly's stack after a successful match.
 *
 * @author Steven J. Metsker, Alan K. Sun
 */
abstract class KAssembler<T> {
    abstract fun workOn(assembly: KAssembly<T>)

    companion object {
        /**
         * Returns a list of the elements on an assembly's stack that appear before a specified fence.
         *
         * Sometimes a parser will recognize a list from within a pair of parentheses or brackets.
         * The parser can mark the beginning of the list with a fence, and then retrieve all the
         * items that come after the fence with this method.
         *
         * @param assembly an assembly whose stack should contain some number of items above a fence marker
         * @param fence the fence, a marker of where to stop popping the stack
         * @return the elements above the specified fence
         */
        fun <T> elementsAbove(assembly: KAssembly<T>, fence: Any): List<Any> {
            val items = mutableListOf<Any>()
            while (!assembly.stackIsEmpty()) {
                val top = assembly.pop()
                if (top == fence) {
                    break
                }
                if (top != null) {
                    items.add(top)
                }
            }
            return items
        }
    }
}

abstract class KTokenAssembler: KAssembler<KToken>()

abstract class KCharAssembler: KAssembler<Char>()