package net.codenest.kassemblix.examples.minimath

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Minimath examples demonstrating minimal arithmetic expression parsing.
 *
 * Converted from: com.sjm.examples.minimath.*
 *
 * Minimath grammar (correct):
 * ```
 *     e = Num m*;
 *     m = '-' Num;
 * ```
 *
 * These examples show:
 * - How to build a recognizer without computation
 * - How to add assemblers for computation
 * - How to use anonymous assemblers (lambdas in Kotlin)
 * - Problems with left recursion
 * - Problems with wrong associativity
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Assemblers
// ============================================================

/**
 * Replaces a token on the stack with its numeric value.
 */
class NumAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        assembly.push(token.nval)
    }
}

/**
 * Pops two numbers and pushes their difference.
 */
class MinusAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 - d1)
    }
}

// ============================================================
// Parser
// ============================================================

/**
 * A parser for Minimath expressions.
 *
 * Grammar:
 * ```
 *     e = Num m*;
 *     m = '-' Num;
 * ```
 */
class MinimathParser {
    private var expr: KSequence<KToken>? = null

    /**
     * Returns a parser for the rule: e = Num m*
     */
    fun e(): KParser<KToken> {
        if (expr == null) {
            expr = KSequence()
            expr!!.add(n())
            expr!!.add(KRepetition(subParser = m()))
        }
        return expr!!
    }

    /**
     * Returns a parser for the rule: m = '-' Num
     */
    private fun m(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KSymbol('-').discard())
            .add(n())
            .also { it.setAssembler(MinusAssembler()) }
    }

    /**
     * Returns a Num parser with assembler to convert token to Double.
     */
    private fun n(): KParser<KToken> {
        return KNum().setAssembler(NumAssembler())
    }

    companion object {
        fun start(): KParser<KToken> = MinimathParser().e()
    }
}

// ============================================================
// Test class
// ============================================================

class MinimathTest {

    /**
     * Show basic recognition without computation.
     *
     * Original: MinimathRecognize.java
     */
    @Test
    fun `show minimath recognition`() {
        val e = KSequence<KToken>()
        e.add(KNum())

        val m = KSequence<KToken>()
        m.add(KSymbol('-'))
        m.add(KNum())

        e.add(KRepetition(subParser = m))

        val result = e.completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        // Stack should contain the tokens
        assertEquals(5, result.getStack().size)  // 3 numbers + 2 minus signs
    }

    /**
     * Show computation with assemblers.
     *
     * Original: MinimathCompute.java
     *
     * Computes 25 - 16 - 9 = 0 (left-associative)
     */
    @Test
    fun `show minimath computation`() {
        val e = KSequence<KToken>()

        val n = KNum().setAssembler(NumAssembler())
        e.add(n)

        val m = KSequence<KToken>()
        m.add(KSymbol('-').discard())
        m.add(n)
        m.setAssembler(MinusAssembler())

        e.add(KRepetition(subParser = m))

        val result = e.completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        assertEquals(0.0, result.pop())  // 25 - 16 - 9 = 0
    }

    /**
     * Show using anonymous assemblers (lambdas in Kotlin).
     *
     * Original: MinimathAnonymous.java
     */
    @Test
    fun `show anonymous assemblers`() {
        val e = KSequence<KToken>()

        // Anonymous assembler for Num
        val n = KNum().setAssembler(object : KTokenAssembler() {
            override fun workOn(assembly: KAssembly<KToken>) {
                val token = assembly.pop() as KToken
                assembly.push(token.nval)
            }
        })
        e.add(n)

        // Anonymous assembler for minus
        val m = KSequence<KToken>()
        m.add(KSymbol('-').discard())
        m.add(n)
        m.setAssembler(object : KTokenAssembler() {
            override fun workOn(assembly: KAssembly<KToken>) {
                val d1 = assembly.pop() as Double
                val d2 = assembly.pop() as Double
                assembly.push(d2 - d1)
            }
        })

        e.add(KRepetition(subParser = m))

        val result = e.completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        assertEquals(0.0, result.pop())
    }

    /**
     * Show using MinimathParser class.
     *
     * Original: MinimathParser.java
     */
    @Test
    fun `show minimath parser class`() {
        val result = MinimathParser.start()
            .completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        assertEquals(0.0, result.pop())
    }

    /**
     * Demonstrate wrong associativity problem.
     *
     * Original: MiniWrongAssociativity.java
     *
     * Grammar:
     *     e = Num '-' e | Num;
     *
     * This grammar is right-associative, so:
     *     25 - 16 - 9 = 25 - (16 - 9) = 25 - 7 = 18 (WRONG!)
     * Instead of:
     *     25 - 16 - 9 = (25 - 16) - 9 = 9 - 9 = 0 (CORRECT)
     */
    @Test
    fun `show wrong associativity`() {
        val e = KAlternation<KToken>()
        val n = KNum().setAssembler(NumAssembler())

        val s = KSequence<KToken>()
        s.add(n)
        s.add(KSymbol('-').discard())
        s.add(e)
        s.setAssembler(MinusAssembler())

        e.add(s)
        e.add(n)

        val result = e.completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        // This gives 18.0 due to right-associativity
        assertEquals(18.0, result.pop())  // WRONG! Should be 0.0
    }

    /**
     * Show correct left-associativity.
     *
     * The proper grammar avoids the problem:
     *     e = Num m*;
     *     m = '-' Num;
     */
    @Test
    fun `show correct left associativity`() {
        val result = MinimathParser.start()
            .completeMatch(KTokenAssembly("25 - 16 - 9"))

        assertNotNull(result)
        assertEquals(0.0, result.pop())  // CORRECT: (25 - 16) - 9 = 0
    }

    /**
     * Note: MiniLeftRecursion.java demonstrates that left-recursive
     * grammars cause infinite loops. We don't test this because it
     * would hang the test.
     *
     * The problematic grammar:
     *     e = Num | e '-' Num;
     *
     * This is left-recursive because 'e' appears at the start of
     * the second alternative. The parser will keep trying to match 'e'
     * without consuming any input, causing an infinite loop.
     */
}
