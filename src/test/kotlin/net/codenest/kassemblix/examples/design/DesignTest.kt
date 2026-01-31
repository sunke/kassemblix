package net.codenest.kassemblix.examples.design

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Parser design pattern examples demonstrating assemblers and stack usage.
 *
 * Converted from: com.sjm.examples.design.*
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Helper classes
// ============================================================

/**
 * A running average tracker. Each number added increases the count by 1,
 * and the total by the amount added.
 */
data class RunningAverage(
    var count: Int = 0,
    var total: Double = 0.0
) {
    /**
     * Add a value to the running average.
     */
    fun add(value: Double) {
        count++
        total += value
    }

    /**
     * Return the average so far.
     */
    fun average(): Double = if (count > 0) total / count else 0.0
}

/**
 * An assembler that updates a running average by adding the length
 * of each word from the stack.
 */
class AverageAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val word = token.sval ?: ""
        val avg = assembly.target as RunningAverage
        avg.add(word.length.toDouble())
    }
}

// ============================================================
// Test class
// ============================================================

class DesignTest {

    /**
     * Show how to use an assembly's stack.
     *
     * Original: ShowStack.java
     *
     * Numbers are pushed onto the stack as they are matched.
     */
    @Test
    fun `show assembly stack usage`() {
        val parser = KRepetition<KToken>(subParser = KNum())
        val assembly = parser.completeMatch(KTokenAssembly("2 4 6 8"))

        assertNotNull(assembly)
        val stack = assembly.getStack()

        // All numbers are on the stack
        assertEquals(4, stack.size)

        // Stack contains the tokens (bottom to top: 2, 4, 6, 8)
        val values = stack.map { (it as KToken).nval }
        assertEquals(listOf(2.0, 4.0, 6.0, 8.0), values)
    }

    /**
     * Show how to use an assembler to calculate the average length
     * of words in a string.
     *
     * Original: ShowAssembler.java
     *
     * This demonstrates using a target object and assembler together.
     */
    @Test
    fun `show assembler calculates average word length`() {
        // As Polonius says in "Hamlet"...
        val quote = "Brevity is the soul of wit"

        val assembly = KTokenAssembly(quote)
        assembly.target = RunningAverage()

        val word = KWord().setAssembler(AverageAssembler())
        val parser = KRepetition<KToken>(subParser = word)

        val result = parser.completeMatch(assembly)

        assertNotNull(result)
        val avg = result.target as RunningAverage

        // Words: "Brevity" (7), "is" (2), "the" (3), "soul" (4), "of" (2), "wit" (3)
        // Total: 21, Count: 6, Average: 3.5
        assertEquals(6, avg.count)
        assertEquals(21.0, avg.total)
        assertEquals(3.5, avg.average())
    }

    /**
     * Show that RunningAverage tracks values correctly.
     */
    @Test
    fun `show running average tracks correctly`() {
        val avg = RunningAverage()

        avg.add(10.0)
        assertEquals(1, avg.count)
        assertEquals(10.0, avg.average())

        avg.add(20.0)
        assertEquals(2, avg.count)
        assertEquals(15.0, avg.average())

        avg.add(30.0)
        assertEquals(3, avg.count)
        assertEquals(20.0, avg.average())
    }

    /**
     * Show that empty RunningAverage returns 0.
     */
    @Test
    fun `show empty running average returns zero`() {
        val avg = RunningAverage()
        assertEquals(0.0, avg.average())
    }
}
