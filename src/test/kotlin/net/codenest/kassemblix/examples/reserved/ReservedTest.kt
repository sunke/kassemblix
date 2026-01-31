package net.codenest.kassemblix.examples.reserved

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Reserved word handling examples.
 *
 * Converted from: com.sjm.examples.reserved.*
 *
 * These examples demonstrate how to handle reserved words in a grammar.
 * Reserved words are special keywords that should be treated differently
 * from regular words.
 *
 * Grammar:
 * ```
 *     query  = (Word | volume)* '?';
 *     volume = "cups" | "gallon" | "liter";
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Reserved Word Terminal
// ============================================================

/**
 * A terminal that matches words from a reserved word list.
 *
 * Unlike a regular Word terminal, this only matches specific
 * known reserved words.
 */
class KReservedWord(private val reservedWords: Set<String>) : KTerminal("ReservedWord") {

    override fun qualify(token: KToken): Boolean {
        return token.isWord() && token.sval in reservedWords
    }

    companion object {
        fun of(vararg words: String) = KReservedWord(words.toSet())
    }
}

/**
 * A terminal that matches words that are NOT in the reserved word list.
 *
 * This is useful when you want regular words to exclude reserved words.
 */
class KNonReservedWord(private val reservedWords: Set<String>) : KTerminal("NonReservedWord") {

    override fun qualify(token: KToken): Boolean {
        return token.isWord() && token.sval !in reservedWords
    }

    companion object {
        fun excluding(vararg words: String) = KNonReservedWord(words.toSet())
    }
}

// ============================================================
// Assemblers
// ============================================================

/**
 * Transforms a volume word into a "VOL(word)" representation.
 */
class VolumeAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop()
        assembly.push("VOL($token)")
    }
}

// ============================================================
// Parser
// ============================================================

/**
 * A parser for volume queries.
 *
 * Grammar:
 * ```
 *     query  = (nonReservedWord | volume)* '?';
 *     volume = "cups" | "gallon" | "liter";
 * ```
 *
 * The volume terminal matches reserved words and transforms them
 * using the VolumeAssembler.
 */
class VolumeQueryParser {
    private val reservedWords = setOf("cups", "gallon", "liter")

    /**
     * Returns a parser for volume queries.
     */
    fun query(): KParser<KToken> {
        val wordOrVolume = KAlternation<KToken>()
            .add(KNonReservedWord.excluding(*reservedWords.toTypedArray()))
            .add(volume())

        return KSequence<KToken>()
            .add(KRepetition(subParser = wordOrVolume))
            .add(KSymbol('?'))
    }

    /**
     * Returns a parser that matches volume reserved words.
     */
    private fun volume(): KParser<KToken> {
        return KReservedWord.of(*reservedWords.toTypedArray())
            .setAssembler(VolumeAssembler())
    }
}

// ============================================================
// Test class
// ============================================================

class ReservedTest {

    /**
     * Show reserved word handling in a query.
     *
     * Original: ShowReserved.java
     *
     * The query "How many cups are in a gallon?" should recognize
     * "cups" and "gallon" as volume reserved words.
     */
    @Test
    fun `show reserved words in query`() {
        val parser = VolumeQueryParser().query()
        val assembly = KTokenAssembly("How many cups are in a gallon ?")

        val result = parser.bestMatch(assembly)

        assertNotNull(result)
        assertTrue(result.remainItemNr() == 0, "Should consume all input")

        // Stack should contain the parsed elements
        val stack = result.getStack()

        // Look for the VOL() wrapped values
        val volItems = stack.filter { it.toString().startsWith("VOL(") }
        assertEquals(2, volItems.size, "Should have 2 volume terms")
        assertTrue(volItems.any { it.toString() == "VOL(cups)" })
        assertTrue(volItems.any { it.toString() == "VOL(gallon)" })
    }

    /**
     * Show that regular words are not treated as reserved.
     */
    @Test
    fun `show regular words not reserved`() {
        val parser = VolumeQueryParser().query()
        val assembly = KTokenAssembly("How many liter bottles ?")

        val result = parser.bestMatch(assembly)

        assertNotNull(result)
        assertTrue(result.remainItemNr() == 0)

        val stack = result.getStack()
        val volItems = stack.filter { it.toString().startsWith("VOL(") }
        assertEquals(1, volItems.size, "Should have 1 volume term (liter)")
        assertTrue(volItems.any { it.toString() == "VOL(liter)" })
    }

    /**
     * Show ReservedWord terminal matching.
     */
    @Test
    fun `show reserved word terminal`() {
        val reserved = KReservedWord.of("cups", "gallon", "liter")

        // Should match reserved words
        val cupsResult = reserved.bestMatch(KTokenAssembly("cups"))
        assertNotNull(cupsResult)
        assertEquals(0, cupsResult.remainItemNr())

        val gallonResult = reserved.bestMatch(KTokenAssembly("gallon"))
        assertNotNull(gallonResult)
        assertEquals(0, gallonResult.remainItemNr())

        // Should not match non-reserved words
        val waterResult = reserved.bestMatch(KTokenAssembly("water"))
        // bestMatch returns the best match, which is null if nothing matches
        assertTrue(waterResult == null || waterResult.remainItemNr() > 0)
    }

    /**
     * Show NonReservedWord terminal matching.
     */
    @Test
    fun `show non-reserved word terminal`() {
        val nonReserved = KNonReservedWord.excluding("cups", "gallon", "liter")

        // Should match non-reserved words
        val waterResult = nonReserved.bestMatch(KTokenAssembly("water"))
        assertNotNull(waterResult)
        assertEquals(0, waterResult.remainItemNr())

        // Should not match reserved words
        val cupsResult = nonReserved.bestMatch(KTokenAssembly("cups"))
        assertTrue(cupsResult == null || cupsResult.remainItemNr() > 0)
    }

    /**
     * Show query with no reserved words.
     */
    @Test
    fun `show query without reserved words`() {
        val parser = VolumeQueryParser().query()
        val assembly = KTokenAssembly("What is the weather today ?")

        val result = parser.bestMatch(assembly)

        assertNotNull(result)
        assertTrue(result.remainItemNr() == 0)

        val stack = result.getStack()
        val volItems = stack.filter { it.toString().startsWith("VOL(") }
        assertEquals(0, volItems.size, "Should have no volume terms")
    }

    /**
     * Show query with all reserved words.
     */
    @Test
    fun `show query with multiple reserved words`() {
        val parser = VolumeQueryParser().query()
        val assembly = KTokenAssembly("cups gallon liter ?")

        val result = parser.bestMatch(assembly)

        assertNotNull(result)
        assertTrue(result.remainItemNr() == 0)

        val stack = result.getStack()
        val volItems = stack.filter { it.toString().startsWith("VOL(") }
        assertEquals(3, volItems.size, "Should have 3 volume terms")
    }
}
