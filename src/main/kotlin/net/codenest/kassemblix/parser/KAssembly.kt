package net.codenest.kassemblix.parser

import java.util.*

/**
 * An assembly provides a parser with a work area.
 */
open class KAssembly<T>(private val delimiter: String = "/") {

    /**
     * An arbitrary target object that assemblers can use to build results.
     * For example, a Coffee parser might set target to a Coffee object.
     */
    var target: Any? = null

    // store the intermediate or final parsing result
    private var resultStack = ArrayDeque<Any>()

    /**
     * Pushes an item onto the result stack.
     * @param t the item to push
     * @return the current assembly
     */
    fun push(t: Any) = apply { resultStack.addLast(t) }

    /**
     * Pops an item from the result stack.
     * @return the popped item or null if the stack is empty
     */
    fun pop(): Any? = resultStack.pollLast()

    /**
     * Returns a copy of the result stack as a list.
     * @return the stack contents as a list (bottom to top)
     */
    fun getStack(): List<Any> = resultStack.toList()

    /**
     * Returns true if the result stack is empty.
     * @return true if the stack is empty
     */
    fun stackIsEmpty(): Boolean = resultStack.isEmpty()


    // store the input items. The items can be tokens or characters
    private var inputItems = ArrayDeque<T>()

    private var nextItemPos = 0

    /**
     * Adds an item to the input items.
     * @param t the item to add
     * @return the current assembly
     */
    fun addItem(t: T) = apply { inputItems.addLast(t) }

    /**
     * Peeks at the next item without consuming it.
     * @return the next item or null if no items remain
     */
    fun peekItem(): T? = inputItems.elementAtOrNull(nextItemPos)

    /**
     * Consumes the next item.
     * @return the next item or null if no items remain
     */
    fun nextItem(): T? = inputItems.elementAtOrNull(nextItemPos++)

    /**
     * Checks if there are more items to consume.
     * @return true if more items remain, false otherwise
     */
    fun hasMoreItem() = nextItemPos < inputItems.size

    /**
     * Returns the number of items that have not been consumed.
     * @return the number of remaining items
     */
    fun remainItemNr() = inputItems.size - nextItemPos

    /**
     * Returns a string of consumed items joined by the delimiter.
     * @return the consumed items as a string
     */
    fun consumedItems(): String = inputItems.take(nextItemPos).joinToString(separator = delimiter)

    /**
     * Returns a string of remaining items joined by the delimiter.
     * @return the remaining items as a string
     */
    fun remainItems(): String = inputItems.drop(nextItemPos).joinToString(separator = delimiter)

    /**
     * Creates a clone of the current assembly.
     * @return the cloned assembly
     */
    fun clone(): KAssembly<T> {
        val clone = KAssembly<T>(this.delimiter)
        clone.target = this.target
        clone.resultStack = ArrayDeque(this.resultStack)
        clone.inputItems = ArrayDeque(this.inputItems)
        clone.nextItemPos = this.nextItemPos
        return clone
    }

    override fun toString() = resultStack.toString() + "|" + consumedItems() + "^" + remainItems()
}