package net.codenest.kassemblix.examples.string

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * String function composition examples demonstrating decorator pattern.
 *
 * Converted from: com.sjm.examples.string.*
 *
 * This example shows how to compose string functions at runtime using
 * either the traditional decorator pattern or Kotlin's functional approach.
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Traditional Decorator Pattern (matching Java implementation)
// ============================================================

/**
 * A StringFunction accepts a string, applies a function, and returns a string.
 * Functions wrap each other to allow runtime composition.
 */
abstract class StringFunction(
    protected val source: StringFunction? = null
) {
    abstract fun f(s: String): String
}

/**
 * Identity function returns the input string unchanged.
 */
class Identity : StringFunction(null) {
    override fun f(s: String): String = s
}

/**
 * Converts string to lowercase.
 */
class LowerCase(source: StringFunction = Identity()) : StringFunction(source) {
    override fun f(s: String): String = source!!.f(s).lowercase()
}

/**
 * Converts string to uppercase.
 */
class UpperCase(source: StringFunction = Identity()) : StringFunction(source) {
    override fun f(s: String): String = source!!.f(s).uppercase()
}

/**
 * Trims whitespace from string.
 */
class Trim(source: StringFunction = Identity()) : StringFunction(source) {
    override fun f(s: String): String = source!!.f(s).trim()
}

/**
 * Extracts a substring.
 */
class Substring(
    source: StringFunction,
    private val from: Int,
    private val to: Int? = null
) : StringFunction(source) {
    override fun f(s: String): String {
        val result = source!!.f(s)
        return if (to == null) {
            result.substring(from)
        } else {
            result.substring(from, to)
        }
    }
}

// ============================================================
// Kotlin Functional Approach
// ============================================================

/**
 * In Kotlin, we can use higher-order functions for composition.
 */
typealias StringFunc = (String) -> String

/**
 * Compose two string functions: apply f first, then g.
 */
infix fun StringFunc.then(g: StringFunc): StringFunc = { s -> g(this(s)) }

// Standard string functions
val identity: StringFunc = { it }
val lowercase: StringFunc = { it.lowercase() }
val uppercase: StringFunc = { it.uppercase() }
val trim: StringFunc = { it.trim() }
fun substring(from: Int, to: Int? = null): StringFunc = { s ->
    if (to == null) s.substring(from) else s.substring(from, to)
}

// ============================================================
// Test class
// ============================================================

class StringFunctionTest {

    /**
     * Show string function composition using decorator pattern.
     *
     * Original: ShowStringFunction.java
     */
    @Test
    fun `show string function composition with decorator pattern`() {
        // Compose: first lowercase, then trim
        val func = Trim(LowerCase())

        val result = func.f(" TAKE IT EASY ")

        assertEquals("take it easy", result)
    }

    /**
     * Show lowercase function.
     */
    @Test
    fun `show lowercase function`() {
        val func = LowerCase()
        assertEquals("hello world", func.f("HELLO WORLD"))
    }

    /**
     * Show uppercase function.
     */
    @Test
    fun `show uppercase function`() {
        val func = UpperCase()
        assertEquals("HELLO WORLD", func.f("hello world"))
    }

    /**
     * Show trim function.
     */
    @Test
    fun `show trim function`() {
        val func = Trim()
        assertEquals("hello", func.f("  hello  "))
    }

    /**
     * Show substring function.
     */
    @Test
    fun `show substring function`() {
        val func = Substring(Identity(), 0, 5)
        assertEquals("hello", func.f("hello world"))

        val func2 = Substring(Identity(), 6)
        assertEquals("world", func2.f("hello world"))
    }

    /**
     * Show complex composition: trim, then lowercase, then substring.
     */
    @Test
    fun `show complex composition`() {
        val func = Substring(LowerCase(Trim()), 0, 4)
        assertEquals("take", func.f(" TAKE IT EASY "))
    }

    // ============================================================
    // Kotlin Functional Approach Tests
    // ============================================================

    /**
     * Show Kotlin functional composition.
     */
    @Test
    fun `show kotlin functional composition`() {
        val func = lowercase then trim

        val result = func(" TAKE IT EASY ")

        assertEquals("take it easy", result)
    }

    /**
     * Show complex functional composition.
     */
    @Test
    fun `show complex functional composition`() {
        val func = trim then lowercase then substring(0, 4)

        assertEquals("take", func(" TAKE IT EASY "))
    }

    /**
     * Show that both approaches produce same result.
     */
    @Test
    fun `show decorator and functional approaches are equivalent`() {
        val input = " HELLO WORLD "

        // Decorator pattern
        val decoratorFunc = Trim(LowerCase())
        val decoratorResult = decoratorFunc.f(input)

        // Functional approach
        val functionalFunc = lowercase then trim
        val functionalResult = functionalFunc(input)

        assertEquals(decoratorResult, functionalResult)
        assertEquals("hello world", decoratorResult)
    }
}
