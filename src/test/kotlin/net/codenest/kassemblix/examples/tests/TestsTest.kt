package net.codenest.kassemblix.examples.tests

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Parser testing examples.
 *
 * Converted from: com.sjm.examples.tests.*
 *
 * These examples demonstrate:
 * - The "dangling else" problem in grammars
 * - Ambiguity detection in parsers
 * - Parser testing strategies
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Dangle Parser - Demonstrates "dangling else" ambiguity
// ============================================================

/**
 * A parser with an ambiguous grammar demonstrating the "dangling else" problem.
 *
 * Grammar:
 * ```
 *     statement     = iff | ifelse | callCustomer | sendBill;
 *     iff           = "if" comparison statement;
 *     ifelse        = "if" comparison statement "else" statement;
 *     comparison    = '(' expression operator expression ')';
 *     expression    = Word | Num;
 *     operator      = '<' | '>' | '=' | "<=" | ">=" | "!=";
 *     callCustomer  = "callCustomer" '(' ')' ';';
 *     sendBill      = "sendBill" '(' ')' ';';
 * ```
 *
 * The ambiguity arises in nested if-else statements like:
 *   if (a < b) if (c < d) callCustomer(); else sendBill();
 *
 * This can be parsed as either:
 *   if (a < b) { if (c < d) callCustomer(); else sendBill(); }
 * or:
 *   if (a < b) { if (c < d) callCustomer(); } else { sendBill(); }
 *
 * Original: Dangle.java
 */
class DangleParser {
    private var statement: KAlternation<KToken>? = null

    fun statement(): KParser<KToken> {
        if (statement == null) {
            statement = KAlternation("<statement>")
            statement!!.add(iff())
            statement!!.add(ifelse())
            statement!!.add(callCustomer())
            statement!!.add(sendBill())
        }
        return statement!!
    }

    private fun iff(): KParser<KToken> {
        return KSequence<KToken>("<iff>")
            .add(KLiteral("if"))
            .add(comparison())
            .add(statement())
    }

    private fun ifelse(): KParser<KToken> {
        return KSequence<KToken>("<ifelse>")
            .add(KLiteral("if"))
            .add(comparison())
            .add(statement())
            .add(KLiteral("else"))
            .add(statement())
    }

    private fun comparison(): KParser<KToken> {
        return KSequence<KToken>("<comparison>")
            .add(KSymbol('('))
            .add(expression())
            .add(operator())
            .add(expression())
            .add(KSymbol(')'))
    }

    private fun expression(): KParser<KToken> {
        return KAlternation<KToken>("<expression>")
            .add(KWord())
            .add(KNum())
    }

    private fun operator(): KParser<KToken> {
        return KAlternation<KToken>("<operator>")
            .add(KSymbol('<'))
            .add(KSymbol('>'))
            .add(KSymbol('='))
            .add(KSymbol("<="))
            .add(KSymbol(">="))
            .add(KSymbol("!="))
    }

    private fun callCustomer(): KParser<KToken> {
        return KSequence<KToken>("<callCustomer>")
            .add(KLiteral("callCustomer"))
            .add(KSymbol('('))
            .add(KSymbol(')'))
            .add(KSymbol(';'))
    }

    private fun sendBill(): KParser<KToken> {
        return KSequence<KToken>("<sendBill>")
            .add(KLiteral("sendBill"))
            .add(KSymbol('('))
            .add(KSymbol(')'))
            .add(KSymbol(';'))
    }
}

// ============================================================
// Volume Query Parser - Demonstrates word/reserved ambiguity
// ============================================================

/**
 * An ambiguous parser where words can match both Word and volume.
 *
 * Grammar:
 * ```
 *     query  = (Word | volume)* '?';
 *     volume = "cups" | "gallon" | "liter";
 * ```
 *
 * The ambiguity arises because "cups", "gallon", and "liter" can
 * match both the Word terminal and the volume parser.
 *
 * Original: VolumeQuery.java
 */
class VolumeQueryParser {
    fun query(): KParser<KToken> {
        val wordOrVolume = KAlternation<KToken>()
            .add(KWord())
            .add(volume())

        return KSequence<KToken>()
            .add(KRepetition(subParser = wordOrVolume))
            .add(KSymbol('?'))
    }

    private fun volume(): KParser<KToken> {
        return KAlternation<KToken>()
            .add(KLiteral("cups"))
            .add(KLiteral("gallon"))
            .add(KLiteral("liter"))
    }
}

// ============================================================
// Test class
// ============================================================

class TestsTest {

    /**
     * Show basic callCustomer statement parses unambiguously.
     */
    @Test
    fun `show simple statement parses`() {
        val parser = DangleParser().statement()
        val result = parser.bestMatch(KTokenAssembly("callCustomer ( ) ;"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show basic sendBill statement parses unambiguously.
     */
    @Test
    fun `show sendBill parses`() {
        val parser = DangleParser().statement()
        val result = parser.bestMatch(KTokenAssembly("sendBill ( ) ;"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show simple if statement parses unambiguously.
     */
    @Test
    fun `show simple if parses`() {
        val parser = DangleParser().statement()
        val result = parser.bestMatch(KTokenAssembly("if ( x < 5 ) callCustomer ( ) ;"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show simple if-else statement parses unambiguously.
     */
    @Test
    fun `show simple if-else parses`() {
        val parser = DangleParser().statement()
        val result = parser.bestMatch(KTokenAssembly("if ( x < 5 ) callCustomer ( ) ; else sendBill ( ) ;"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Demonstrate the dangling else ambiguity.
     *
     * Original: ShowDangleTest.java
     *
     * The statement:
     *   if (a < b) if (c < d) callCustomer(); else sendBill();
     *
     * Can be parsed two ways - the "dangling else" problem.
     */
    @Test
    fun `show dangling else ambiguity`() {
        val parser = DangleParser().statement()
        val input = "if ( a < b ) if ( c < d ) callCustomer ( ) ; else sendBill ( ) ;"

        // This should throw ambiguity exception because both
        // iff and ifelse match the input differently
        assertThrows<Exception> {
            parser.bestMatch(KTokenAssembly(input))
        }
    }

    /**
     * Show that volume query is ambiguous.
     *
     * Original: ShowVolumeTest.java
     *
     * The word "cups" can match both Word and volume("cups").
     */
    @Test
    fun `show volume query ambiguity`() {
        val parser = VolumeQueryParser().query()

        // "cups ?" is ambiguous - cups can match Word or volume
        assertThrows<Exception> {
            parser.bestMatch(KTokenAssembly("cups ?"))
        }
    }

    /**
     * Show non-volume words parse unambiguously.
     */
    @Test
    fun `show non-volume words parse unambiguously`() {
        val parser = VolumeQueryParser().query()

        // "water ?" is not ambiguous - water only matches Word
        val result = parser.bestMatch(KTokenAssembly("water ?"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show multiple non-volume words parse.
     */
    @Test
    fun `show multiple non-volume words`() {
        val parser = VolumeQueryParser().query()
        val result = parser.bestMatch(KTokenAssembly("how much water ?"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show comparison operators in Dangle grammar.
     */
    @Test
    fun `show comparison operators`() {
        val parser = DangleParser().statement()

        val operators = listOf("<", ">", "=", "<=", ">=", "!=")
        for (op in operators) {
            val input = "if ( x $op 5 ) callCustomer ( ) ;"
            val result = parser.bestMatch(KTokenAssembly(input))
            assertNotNull(result, "Should parse with operator $op")
            assertEquals(0, result.remainItemNr(), "Should consume all with operator $op")
        }
    }

    /**
     * Show numbers work in comparisons.
     */
    @Test
    fun `show numbers in comparisons`() {
        val parser = DangleParser().statement()
        val result = parser.bestMatch(KTokenAssembly("if ( 10 < 20 ) callCustomer ( ) ;"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Note: The original Java code uses ParserTester/TokenTester to generate
     * random valid inputs and verify the parser can handle them unambiguously.
     * This requires implementing randomInput() on all parser classes, which
     * is beyond the scope of this example conversion.
     *
     * The key concepts demonstrated here are:
     * 1. The "dangling else" problem in grammars
     * 2. Ambiguity when reserved words overlap with generic terminals
     * 3. How the bestMatch() method detects ambiguity
     */
}
