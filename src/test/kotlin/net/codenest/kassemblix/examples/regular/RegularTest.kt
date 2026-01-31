package net.codenest.kassemblix.examples.regular

import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regular expression parser examples.
 *
 * Converted from: com.sjm.examples.regular.*
 *
 * This is a meta-parser that parses regular expressions and builds
 * parsers from them. For example, parsing "a*" produces a parser
 * that matches strings of zero or more 'a' characters.
 *
 * Grammar:
 * ```
 *     expression    = term orTerm*;
 *     term          = factor nextFactor*;
 *     orTerm        = '|' term;
 *     factor        = phrase | phraseStar;
 *     nextFactor    = factor;
 *     phrase        = letterOrDigit | '(' expression ')';
 *     phraseStar    = phrase '*';
 *     letterOrDigit = Letter | Digit;
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Exception
// ============================================================

class RegularExpressionException(message: String) : Exception(message)

// ============================================================
// Assemblers
// ============================================================

/**
 * Pops a Character and pushes a KSpecificChar parser.
 */
class CharAssembler : KCharAssembler() {
    override fun workOn(assembly: KAssembly<Char>) {
        val ch = assembly.pop() as Char
        assembly.push(KSpecificChar(ch))
    }
}

/**
 * Pops two parsers and pushes a Sequence of them.
 */
class AndAssembler : KCharAssembler() {
    override fun workOn(assembly: KAssembly<Char>) {
        val top = assembly.pop()
        @Suppress("UNCHECKED_CAST")
        val first = assembly.pop() as KParser<Char>
        @Suppress("UNCHECKED_CAST")
        val second = top as KParser<Char>
        val seq = KSequence<Char>()
        seq.add(first)
        seq.add(second)
        assembly.push(seq)
    }
}

/**
 * Pops two parsers and pushes an Alternation of them.
 */
class OrAssembler : KCharAssembler() {
    override fun workOn(assembly: KAssembly<Char>) {
        val top = assembly.pop()
        @Suppress("UNCHECKED_CAST")
        val first = assembly.pop() as KParser<Char>
        @Suppress("UNCHECKED_CAST")
        val second = top as KParser<Char>
        val alt = KAlternation<Char>()
        alt.add(first)
        alt.add(second)
        assembly.push(alt)
    }
}

/**
 * Pops a parser and pushes a Repetition of it.
 */
class StarAssembler : KCharAssembler() {
    override fun workOn(assembly: KAssembly<Char>) {
        @Suppress("UNCHECKED_CAST")
        val p = assembly.pop() as KParser<Char>
        assembly.push(KRepetition(subParser = p))
    }
}

// ============================================================
// Parser
// ============================================================

/**
 * A parser that recognizes regular expressions and builds parsers from them.
 */
class RegularParser {
    private var expr: KSequence<Char>? = null

    /**
     * Returns a parser for: expression = term orTerm*
     */
    fun expression(): KParser<Char> {
        if (expr == null) {
            expr = KSequence()
            expr!!.add(term())
            expr!!.add(KRepetition(subParser = orTerm()))
        }
        return expr!!
    }

    /**
     * Returns a parser for: factor = phraseStar | phrase
     * Note: phraseStar first to prefer longer match
     */
    private fun factor(): KParser<Char> {
        return KAlternation<Char>()
            .add(phraseStar())
            .add(phrase())
    }

    /**
     * Returns a parser for: letterOrDigit = Letter | Digit
     */
    private fun letterOrDigit(): KParser<Char> {
        return KAlternation<Char>()
            .add(KLetter())
            .add(KDigit())
            .also { it.setAssembler(CharAssembler()) }
    }

    /**
     * Returns a parser for: nextFactor = factor
     */
    private fun nextFactor(): KParser<Char> {
        return factor().also { it.setAssembler(AndAssembler()) }
    }

    /**
     * Returns a parser for: orTerm = '|' term
     */
    private fun orTerm(): KParser<Char> {
        return KSequence<Char>()
            .add(KSpecificChar('|').discard())
            .add(term())
            .also { it.setAssembler(OrAssembler()) }
    }

    /**
     * Returns a parser for: phrase = letterOrDigit | '(' expression ')'
     */
    private fun phrase(): KParser<Char> {
        val parenExpr = KSequence<Char>()
            .add(KSpecificChar('(').discard())
            .add(expression())
            .add(KSpecificChar(')').discard())

        return KAlternation<Char>()
            .add(letterOrDigit())
            .add(parenExpr)
    }

    /**
     * Returns a parser for: phraseStar = phrase '*'
     */
    private fun phraseStar(): KParser<Char> {
        return KSequence<Char>()
            .add(phrase())
            .add(KSpecificChar('*').discard())
            .also { it.setAssembler(StarAssembler()) }
    }

    /**
     * Returns a parser for: term = factor nextFactor*
     */
    private fun term(): KParser<Char> {
        return KSequence<Char>()
            .add(factor())
            .add(KRepetition(subParser = nextFactor()))
    }

    companion object {
        fun start(): KParser<Char> = RegularParser().expression()

        /**
         * Parse a regular expression string and return a parser.
         */
        @Suppress("UNCHECKED_CAST")
        fun value(s: String): KParser<Char> {
            val assembly = KCharAssembly(s)
            val result = start().completeMatch(assembly)
                ?: throw RegularExpressionException("Improperly formed regular expression: $s")

            return try {
                result.pop() as KParser<Char>
            } catch (e: Exception) {
                throw RegularExpressionException("Internal error in RegularParser: ${e.message}")
            }
        }
    }
}

// ============================================================
// Test class
// ============================================================

class RegularTest {

    /**
     * Helper to check if a parser matches a string.
     * Uses bestMatch to avoid ambiguity exceptions.
     */
    private fun matches(parser: KParser<Char>, s: String): Boolean {
        val assembly = KCharAssembly(s)
        val result = parser.bestMatch(assembly)
        return result != null && result.remainItemNr() == 0
    }

    /**
     * Show basic character matching: a*
     *
     * Original: ShowRegularParser.java
     */
    @Test
    fun `show a star matches zero or more a`() {
        val aStar = RegularParser.value("a*")

        assertTrue(matches(aStar, ""))       // zero a's
        assertTrue(matches(aStar, "a"))      // one a
        assertTrue(matches(aStar, "aa"))     // two a's
        assertTrue(matches(aStar, "aaaa"))   // many a's
    }

    /**
     * Show alternation: (a|b)*
     */
    @Test
    fun `show alternation a or b star`() {
        val abStar = RegularParser.value("(a|b)*")

        assertTrue(matches(abStar, ""))
        assertTrue(matches(abStar, "a"))
        assertTrue(matches(abStar, "b"))
        assertTrue(matches(abStar, "ab"))
        assertTrue(matches(abStar, "aabbab"))
    }

    /**
     * Show simple letter matching.
     */
    @Test
    fun `show simple letter matching`() {
        val a = RegularParser.value("a")
        assertTrue(matches(a, "a"))

        val b = RegularParser.value("b")
        assertTrue(matches(b, "b"))
    }

    /**
     * Show simple alternation: a|b
     */
    @Test
    fun `show simple alternation`() {
        val aOrB = RegularParser.value("a|b")
        assertTrue(matches(aOrB, "a"))
        assertTrue(matches(aOrB, "b"))
    }

    /**
     * Show direct use of character parsers.
     */
    @Test
    fun `show direct character parsers`() {
        // Four letters using direct parser construction
        val l4 = KSequence<Char>()
            .add(KLetter())
            .add(KLetter())
            .add(KLetter())
            .add(KLetter())

        assertTrue(matches(l4, "java"))
        assertTrue(matches(l4, "abcd"))
    }

    /**
     * Show repetition of letters.
     */
    @Test
    fun `show letter repetition`() {
        val letters = KRepetition<Char>(subParser = KLetter())

        assertTrue(matches(letters, ""))
        assertTrue(matches(letters, "a"))
        assertTrue(matches(letters, "coffee"))
    }

    /**
     * Show digit matching.
     */
    @Test
    fun `show digit in regex`() {
        val digit = RegularParser.value("1")
        assertTrue(matches(digit, "1"))
    }

    /**
     * Show the regex parser itself works.
     */
    @Test
    fun `show regex parser parses expressions`() {
        // These should all parse without exception
        assertNotNull(RegularParser.value("a"))
        assertNotNull(RegularParser.value("a*"))
        assertNotNull(RegularParser.value("a|b"))
        assertNotNull(RegularParser.value("(a)"))
        assertNotNull(RegularParser.value("(a|b)*"))
    }
}
