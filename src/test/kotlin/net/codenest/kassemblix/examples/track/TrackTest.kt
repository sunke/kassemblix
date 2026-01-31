package net.codenest.kassemblix.examples.track

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Track examples demonstrating error tracking in sequences.
 *
 * Converted from: com.sjm.examples.track.*
 *
 * A Track is a sequence that throws a TrackException if the sequence
 * begins but does not complete. This provides better error messages
 * by indicating where parsing failed.
 *
 * Grammar:
 * ```
 *     list       = '(' contents ')';
 *     contents   = empty | actualList;
 *     actualList = Word (',' Word)*;
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Exception
// ============================================================

/**
 * Signals that a parser could not match text after a specific point.
 *
 * @param after     what text was successfully parsed
 * @param expected  what kind of thing was expected
 * @param found     the text actually found
 */
class TrackException(
    val after: String,
    val expected: String,
    val found: String
) : RuntimeException(
    "After   : $after\nExpected: $expected\nFound   : $found"
)

// ============================================================
// Track Parser
// ============================================================

/**
 * A Track is a sequence that throws a TrackException if the sequence
 * begins but does not complete.
 *
 * This is useful for providing better error messages when parsing
 * structured input like lists or function calls.
 */
class KTrack<T>(name: String = "") : KParser<T>("Track $name") {

    private val subParsers = mutableListOf<KParser<T>>()

    fun add(parser: KParser<T>) = apply { subParsers.add(parser) }

    override fun match(assemblies: List<KAssembly<T>>): List<KAssembly<T>> {
        var inTrack = false
        var last = assemblies
        var out = assemblies

        for (p in subParsers) {
            out = p.matchAndAssemble(last)
            if (out.isEmpty()) {
                if (inTrack) {
                    throwTrackException(last, p)
                }
                return out
            }
            inTrack = true
            last = out
        }
        return out
    }

    private fun throwTrackException(previousState: List<KAssembly<T>>, p: KParser<T>) {
        val best = previousState.minByOrNull { it.remainItemNr() }
            ?: throw TrackException("-nothing-", p.toString(), "-nothing-")

        val after = best.consumedItems().ifEmpty { "-nothing-" }
        val expected = p.toString()
        val found = best.peekItem()?.toString() ?: "-nothing-"

        throw TrackException(after, expected, found)
    }
}

// ============================================================
// List Parser
// ============================================================

/**
 * A parser for comma-separated lists in parentheses.
 *
 * Grammar:
 * ```
 *     list       = '(' contents ')';
 *     contents   = empty | actualList;
 *     actualList = Word (',' Word)*;
 * ```
 */
class ListParser {

    fun list(): KParser<KToken> {
        // commaWord = ',' Word
        val commaWord = KTrack<KToken>()
            .add(KSymbol(',').discard())
            .add(KWord())

        // actualList = Word (',' Word)*
        val actualList = KSequence<KToken>()
            .add(KWord())
            .add(KRepetition(subParser = commaWord))

        // contents = empty | actualList
        val contents = KAlternation<KToken>()
            .add(KEmpty())
            .add(actualList)

        // list = '(' contents ')'
        return KTrack<KToken>()
            .add(KSymbol('(').discard())
            .add(contents)
            .add(KSymbol(')').discard())
    }
}

// ============================================================
// Test class
// ============================================================

class TrackTest {

    private val parser = ListParser().list()

    /**
     * Show empty list parsing.
     *
     * Original: ShowTrack.java - "()"
     */
    @Test
    fun `show empty list`() {
        val result = parser.completeMatch(KTokenAssembly("( )"))

        assertNotNull(result)
        assertTrue(result.getStack().isEmpty(), "Empty list should have empty stack")
    }

    /**
     * Show single item list.
     *
     * Original: ShowTrack.java - "(pilfer)"
     */
    @Test
    fun `show single item list`() {
        val result = parser.completeMatch(KTokenAssembly("( pilfer )"))

        assertNotNull(result)
        assertEquals(1, result.getStack().size)
    }

    /**
     * Show two item list.
     *
     * Original: ShowTrack.java - "(pilfer, pinch)"
     */
    @Test
    fun `show two item list`() {
        val result = parser.completeMatch(KTokenAssembly("( pilfer , pinch )"))

        assertNotNull(result)
        assertEquals(2, result.getStack().size)
    }

    /**
     * Show three item list.
     *
     * Original: ShowTrack.java - "(pilfer, pinch, purloin)"
     */
    @Test
    fun `show three item list`() {
        val result = parser.completeMatch(KTokenAssembly("( pilfer , pinch , purloin )"))

        assertNotNull(result)
        assertEquals(3, result.getStack().size)
    }

    /**
     * Show error with double comma.
     *
     * Original: ShowTrack.java - "(pilfer, pinch,, purloin)"
     *
     * Should throw TrackException because after ',' we expect a Word.
     */
    @Test
    fun `show error with double comma`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( pilfer , pinch , , purloin )"))
        }

        assertTrue(exception.message!!.contains("Expected"))
        assertEquals(",", exception.found)
    }

    /**
     * Show error with unclosed parenthesis - single opening.
     *
     * Original: ShowTrack.java - "("
     */
    @Test
    fun `show error with unclosed parenthesis`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("("))
        }

        assertTrue(exception.message!!.contains("Expected"))
    }

    /**
     * Show error with unclosed parenthesis and content.
     *
     * Original: ShowTrack.java - "(pilfer"
     */
    @Test
    fun `show error with unclosed parenthesis and content`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( pilfer"))
        }

        assertTrue(exception.message!!.contains("Expected"))
        assertEquals("-nothing-", exception.found)
    }

    /**
     * Show error with trailing comma.
     *
     * Original: ShowTrack.java - "(pilfer, "
     */
    @Test
    fun `show error with trailing comma`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( pilfer ,"))
        }

        assertTrue(exception.message!!.contains("Expected"))
    }

    /**
     * Show error with leading comma.
     *
     * Original: ShowTrack.java - "(, pinch, purloin)"
     */
    @Test
    fun `show error with leading comma`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( , pinch , purloin )"))
        }

        assertTrue(exception.message!!.contains("Expected"))
    }

    /**
     * Show error without parentheses (returns null, no exception).
     *
     * Original: ShowTrack.java - "pilfer, pinch"
     *
     * This should return null because parsing never enters the track
     * (no opening parenthesis).
     */
    @Test
    fun `show no match without parentheses`() {
        val result = parser.bestMatch(KTokenAssembly("pilfer , pinch"))

        // Should return null (no match) or have remaining items
        // because the track was never entered (no opening paren)
        assertTrue(result == null || result.remainItemNr() > 0)
    }

    /**
     * Show TrackException provides useful error details.
     */
    @Test
    fun `show track exception details`() {
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( pilfer , , )"))
        }

        // Check that we have meaningful error information
        assertNotNull(exception.after)
        assertNotNull(exception.expected)
        assertNotNull(exception.found)

        // The found element should be the unexpected comma
        assertEquals(",", exception.found)
    }

    /**
     * Show nested Track behavior with commaWord.
     */
    @Test
    fun `show nested track error`() {
        // This tests the inner track (commaWord) throwing exception
        val exception = assertThrows<TrackException> {
            parser.completeMatch(KTokenAssembly("( a , )"))
        }

        // After consuming "(", "a", ",", expected Word but found ")"
        assertEquals(")", exception.found)
    }
}
