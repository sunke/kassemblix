package net.codenest.kassemblix.examples.midimath

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Midimath examples demonstrating operator precedence and cyclic grammar handling.
 *
 * Converted from: com.sjm.examples.midimath.*
 *
 * Midimath grammar:
 * ```
 *     expression = term ('+' term)*;
 *     term       = Num ('*' Num)*;
 * ```
 *
 * Extended MidiParser grammar:
 * ```
 *     expression = term ('-' term)*;
 *     term       = '(' expression ')' | Num;
 * ```
 *
 * These examples show:
 * - Operator precedence (* before +)
 * - Cyclic grammar handling (avoiding infinite loops during construction)
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
 * Pops two numbers and pushes their sum.
 */
class PlusAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 + d1)
    }
}

/**
 * Pops two numbers and pushes their product.
 */
class TimesAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 * d1)
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
// Parsers
// ============================================================

/**
 * A parser for Midimath expressions with + and *.
 *
 * Grammar:
 * ```
 *     expression = term ('+' term)*;
 *     term       = Num ('*' Num)*;
 * ```
 *
 * This shows operator precedence: * is evaluated before +.
 */
class MidimathParser {

    /**
     * Returns parser for: expression = term ('+' term)*
     */
    fun expression(): KParser<KToken> {
        val plusTerm = KSequence<KToken>()
            .add(KSymbol('+').discard())
            .add(term())
        plusTerm.setAssembler(PlusAssembler())

        return KSequence<KToken>()
            .add(term())
            .add(KRepetition(subParser = plusTerm))
    }

    /**
     * Returns parser for: term = Num ('*' Num)*
     */
    private fun term(): KParser<KToken> {
        val n = KNum().setAssembler(NumAssembler())

        val timesNum = KSequence<KToken>()
            .add(KSymbol('*').discard())
            .add(n)
        timesNum.setAssembler(TimesAssembler())

        return KSequence<KToken>()
            .add(n)
            .add(KRepetition(subParser = timesNum))
    }
}

/**
 * A parser with parentheses support - avoids cyclic construction.
 *
 * Grammar:
 * ```
 *     expression = term ('-' term)*;
 *     term       = '(' expression ')' | Num;
 * ```
 *
 * Uses lazy initialization to avoid infinite recursion during construction.
 */
class MidiParser {
    private var expr: KSequence<KToken>? = null

    /**
     * Returns parser for: expression = term ('-' term)*
     */
    fun expression(): KParser<KToken> {
        if (expr == null) {
            expr = KSequence()
            expr!!.add(term())
            expr!!.add(KRepetition(subParser = minusTerm()))
        }
        return expr!!
    }

    /**
     * Returns parser for: minusTerm = '-' term
     */
    private fun minusTerm(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KSymbol('-').discard())
            .add(term())
            .also { it.setAssembler(MinusAssembler()) }
    }

    /**
     * Returns parser for: term = '(' expression ')' | Num
     */
    private fun term(): KParser<KToken> {
        val parenExpr = KSequence<KToken>()
            .add(KSymbol('(').discard())
            .add(expression())
            .add(KSymbol(')').discard())

        return KAlternation<KToken>()
            .add(parenExpr)
            .add(KNum().setAssembler(NumAssembler()))
    }
}

/**
 * Extended parser with + and * and parentheses.
 *
 * Grammar:
 * ```
 *     expression = term ('+' term)*;
 *     term       = factor ('*' factor)*;
 *     factor     = '(' expression ')' | Num;
 * ```
 */
class MidiloopNotParser {
    private var expr: KSequence<KToken>? = null

    fun expression(): KParser<KToken> {
        if (expr == null) {
            expr = KSequence()

            val plusTerm = KSequence<KToken>()
                .add(KSymbol('+').discard())
                .add(term())
            plusTerm.setAssembler(PlusAssembler())

            expr!!.add(term())
            expr!!.add(KRepetition(subParser = plusTerm))
        }
        return expr!!
    }

    private fun factor(): KParser<KToken> {
        val parenExpr = KSequence<KToken>()
            .add(KSymbol('(').discard())
            .add(expression())
            .add(KSymbol(')').discard())

        return KAlternation<KToken>()
            .add(parenExpr)
            .add(KNum().setAssembler(NumAssembler()))
    }

    private fun term(): KParser<KToken> {
        val timesFactor = KSequence<KToken>()
            .add(KSymbol('*').discard())
            .add(factor())
        timesFactor.setAssembler(TimesAssembler())

        return KSequence<KToken>()
            .add(factor())
            .add(KRepetition(subParser = timesFactor))
    }
}

// ============================================================
// Test class
// ============================================================

class MidimathTest {

    /**
     * Show operator precedence: * before +
     *
     * Original: Midimath.java
     *
     * 2 + 3 * 7 + 19 = 2 + 21 + 19 = 42
     */
    @Test
    fun `show operator precedence`() {
        val result = MidimathParser().expression()
            .bestMatch(KTokenAssembly("2 + 3 * 7 + 19"))

        assertNotNull(result)
        assertEquals(42.0, result.pop())
    }

    /**
     * Show simple addition.
     */
    @Test
    fun `show simple addition`() {
        val result = MidimathParser().expression()
            .bestMatch(KTokenAssembly("5 + 3"))

        assertNotNull(result)
        assertEquals(8.0, result.pop())
    }

    /**
     * Show simple multiplication.
     */
    @Test
    fun `show simple multiplication`() {
        val result = MidimathParser().expression()
            .bestMatch(KTokenAssembly("4 * 3"))

        assertNotNull(result)
        assertEquals(12.0, result.pop())
    }

    /**
     * Show MidiParser with parentheses.
     *
     * Original: MidiParser.java
     *
     * 111 - (11 - 1) = 111 - 10 = 101
     */
    @Test
    fun `show parser with parentheses`() {
        val result = MidiParser().expression()
            .bestMatch(KTokenAssembly("111 - (11 - 1)"))

        assertNotNull(result)
        assertEquals(101.0, result.pop())
    }

    /**
     * Show extended parser with + * and parentheses.
     *
     * Original: MidiloopNot.java
     *
     * (7 + 13) * 5 = 20 * 5 = 100
     */
    @Test
    fun `show extended parser with parentheses`() {
        val result = MidiloopNotParser().expression()
            .bestMatch(KTokenAssembly("(7 + 13) * 5"))

        assertNotNull(result)
        assertEquals(100.0, result.pop())
    }

    /**
     * Show complex expression with all operators.
     *
     * (2 + 3) * (4 + 1) = 5 * 5 = 25
     */
    @Test
    fun `show complex expression`() {
        val result = MidiloopNotParser().expression()
            .bestMatch(KTokenAssembly("(2 + 3) * (4 + 1)"))

        assertNotNull(result)
        assertEquals(25.0, result.pop())
    }

    /**
     * Note: Midiloop.java demonstrates that careless grammar implementation
     * causes infinite loop during construction. We don't test this because
     * it would hang the test.
     *
     * The problem: In Midiloop, expression() calls term(), which calls factor(),
     * which calls expression() again - all during object construction.
     * This creates infinite recursion.
     *
     * The solution (MidiloopNot): Use lazy initialization with null check
     * to ensure expression is only constructed once.
     */
}
