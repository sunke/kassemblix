package net.codenest.kassemblix.examples.mechanics

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Parser mechanics examples demonstrating how parsers work internally.
 *
 * Converted from: com.sjm.examples.mechanics.*
 *
 * These examples demonstrate:
 * - Sequence matching
 * - Repetition matching
 * - Alternation matching
 * - Best match vs complete match
 * - Custom terminals
 * - Push/discard behavior
 * - Cyclic grammar handling
 * - Ambiguity detection
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Custom Terminals
// ============================================================

/**
 * A terminal that matches only lowercase words.
 *
 * Original: LowercaseWord.java
 */
class KLowercaseWord : KTerminal("LowercaseWord") {
    override fun qualify(token: KToken): Boolean {
        if (!token.isWord()) return false
        val word = token.sval ?: return false
        return word.isNotEmpty() && word[0].isLowerCase()
    }
}

/**
 * A terminal that matches only uppercase (capitalized) words.
 *
 * Original: UppercaseWord.java
 */
class KUppercaseWord : KTerminal("UppercaseWord") {
    override fun qualify(token: KToken): Boolean {
        if (!token.isWord()) return false
        val word = token.sval ?: return false
        return word.isNotEmpty() && word[0].isUpperCase()
    }
}

// ============================================================
// Test class
// ============================================================

class MechanicsTest {

    // --------------------------------------------------------
    // Sequence Tests
    // --------------------------------------------------------

    /**
     * Show basic sequence matching.
     *
     * Original: ShowSequenceSimple.java
     */
    @Test
    fun `show sequence simple`() {
        val hello = KLiteral("Hello")
        val world = KLiteral("world")
        val bang = KSymbol('!')

        val seq = KSequence<KToken>()
            .add(hello)
            .add(world)
            .add(bang)

        val result = seq.bestMatch(KTokenAssembly("Hello world !"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show sequence with leftovers.
     *
     * Original: ShowSequenceLeftovers.java
     */
    @Test
    fun `show sequence with leftovers`() {
        val seq = KSequence<KToken>()
            .add(KWord())
            .add(KWord())

        val result = seq.bestMatch(KTokenAssembly("hello world and more"))

        assertNotNull(result)
        assertEquals(2, result.remainItemNr()) // "and more" remain
    }

    // --------------------------------------------------------
    // Repetition Tests
    // --------------------------------------------------------

    /**
     * Show repetition creates multiple match states.
     *
     * Original: ShowRepetitionMatch.java
     */
    @Test
    fun `show repetition match`() {
        val parser = KRepetition<KToken>(subParser = KWord())
        val assembly = KTokenAssembly("How many cups are in a gallon")

        // Using match() directly shows all possible states
        val states = parser.matchAndAssemble(listOf(assembly))

        // Repetition creates multiple states: 0 words, 1 word, 2 words, etc.
        assertTrue(states.size > 1)
    }

    /**
     * Show repetition best match consumes all.
     */
    @Test
    fun `show repetition best match`() {
        val parser = KRepetition<KToken>(subParser = KWord())
        val result = parser.bestMatch(KTokenAssembly("hello world"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    // --------------------------------------------------------
    // Alternation Tests
    // --------------------------------------------------------

    /**
     * Show basic alternation.
     *
     * Original: ShowAlternationBasic.java
     */
    @Test
    fun `show alternation basic`() {
        val alt = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        // Single alternation matches first token
        val result = alt.bestMatch(KTokenAssembly("hot hot steaming hot coffee"))

        assertNotNull(result)
        assertEquals(4, result.remainItemNr()) // matched "hot", 4 tokens remain
    }

    /**
     * Show alternation with repetition.
     */
    @Test
    fun `show alternation repetition`() {
        val alt = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        val rep = KRepetition(subParser = alt)
        val result = rep.bestMatch(KTokenAssembly("hot hot steaming hot coffee"))

        assertNotNull(result)
        assertEquals(1, result.remainItemNr()) // "coffee" remains
    }

    // --------------------------------------------------------
    // Best Match vs Complete Match
    // --------------------------------------------------------

    /**
     * Show best match returns partial result.
     *
     * Original: ShowBestMatch.java
     */
    @Test
    fun `show best match partial`() {
        val alt = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        val rep = KRepetition(subParser = alt)
        val result = rep.bestMatch(KTokenAssembly("hot hot steaming hot coffee"))

        assertNotNull(result)
        assertTrue(result.remainItemNr() > 0) // "coffee" doesn't match
    }

    /**
     * Show complete match returns null when input not fully consumed.
     *
     * Original: ShowCompleteMatch.java
     */
    @Test
    fun `show complete match requires full consumption`() {
        val alt = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        val rep = KRepetition(subParser = alt)

        // Complete match should return null because "coffee" doesn't match
        val result = rep.completeMatch(KTokenAssembly("hot hot steaming hot coffee"))
        assertNull(result)
    }

    /**
     * Show complete match succeeds when input fully consumed.
     */
    @Test
    fun `show complete match succeeds`() {
        val alt = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        val rep = KRepetition(subParser = alt)
        val result = rep.completeMatch(KTokenAssembly("hot hot steaming hot"))

        assertNotNull(result)
    }

    // --------------------------------------------------------
    // Custom Terminals
    // --------------------------------------------------------

    /**
     * Show custom terminals for uppercase/lowercase words.
     *
     * Original: ShowNewTerminals.java
     */
    @Test
    fun `show new terminals`() {
        val variable = KUppercaseWord()
        val known = KLowercaseWord()

        val term = KAlternation<KToken>()
            .add(variable.also { it.setAssembler(object : KTokenAssembler() {
                override fun workOn(assembly: KAssembly<KToken>) {
                    val o = assembly.pop()
                    assembly.push("VAR($o)")
                }
            })})
            .add(known.also { it.setAssembler(object : KTokenAssembler() {
                override fun workOn(assembly: KAssembly<KToken>) {
                    val o = assembly.pop()
                    assembly.push("KNOWN($o)")
                }
            })})

        val result = KRepetition(subParser = term)
            .bestMatch(KTokenAssembly("member X republican democrat"))

        assertNotNull(result)
        val stack = result.getStack()
        assertTrue(stack.any { it.toString().contains("KNOWN(member)") })
        assertTrue(stack.any { it.toString().contains("VAR(X)") })
        assertTrue(stack.any { it.toString().contains("KNOWN(republican)") })
        assertTrue(stack.any { it.toString().contains("KNOWN(democrat)") })
    }

    /**
     * Show lowercase word matching.
     */
    @Test
    fun `show lowercase word`() {
        val lower = KLowercaseWord()

        val result1 = lower.bestMatch(KTokenAssembly("hello"))
        assertNotNull(result1)
        assertEquals(0, result1.remainItemNr())

        val result2 = lower.bestMatch(KTokenAssembly("Hello"))
        assertTrue(result2 == null || result2.remainItemNr() > 0)
    }

    /**
     * Show uppercase word matching.
     */
    @Test
    fun `show uppercase word`() {
        val upper = KUppercaseWord()

        val result1 = upper.bestMatch(KTokenAssembly("Hello"))
        assertNotNull(result1)
        assertEquals(0, result1.remainItemNr())

        val result2 = upper.bestMatch(KTokenAssembly("hello"))
        assertTrue(result2 == null || result2.remainItemNr() > 0)
    }

    // --------------------------------------------------------
    // Push/Discard
    // --------------------------------------------------------

    /**
     * Show discard prevents pushing to stack.
     *
     * Original: ShowPush.java
     */
    @Test
    fun `show push and discard`() {
        val open = KSymbol('(').discard()
        val close = KSymbol(')').discard()
        val comma = KSymbol(',').discard()
        val num = KNum()

        val coord = KSequence<KToken>()
            .add(open)
            .add(num).add(comma).add(num).add(comma).add(num)
            .add(close)

        val result = coord.bestMatch(KTokenAssembly("( 23.4 , 34.5 , 45.6 )"))

        assertNotNull(result)
        val stack = result.getStack()

        // Only numbers should be on stack (parens and commas discarded)
        assertEquals(3, stack.size)
    }

    // --------------------------------------------------------
    // Cyclic Grammar
    // --------------------------------------------------------

    /**
     * Show cyclic grammar handling.
     *
     * Original: ShowCycle.java
     */
    @Test
    fun `show cyclic grammar`() {
        // ticks = "tick" | "tick" ticks
        val ticks = KAlternation<KToken>()
        val tick = KLiteral("tick")

        ticks
            .add(tick)
            .add(KSequence<KToken>().add(tick).add(ticks))

        val result = ticks.bestMatch(KTokenAssembly("tick tick tick tick"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    // --------------------------------------------------------
    // Ambiguity
    // --------------------------------------------------------

    /**
     * Show ambiguity detection.
     *
     * Original: ShowAmbiguity.java
     */
    @Test
    fun `show ambiguity detection`() {
        // volume = "cups" | "gallon" | "liter"
        val volume = KAlternation<KToken>()
            .add(KLiteral("cups"))
            .add(KLiteral("gallon"))
            .add(KLiteral("liter"))

        volume.setAssembler(object : KTokenAssembler() {
            override fun workOn(assembly: KAssembly<KToken>) {
                val o = assembly.pop()
                assembly.push("VOL($o)")
            }
        })

        // query = (Word | volume)* '?'
        // This is ambiguous because "cups" matches both Word and volume
        val wordOrVolume = KAlternation<KToken>()
            .add(KWord())
            .add(volume)

        val query = KSequence<KToken>()
            .add(KRepetition(subParser = wordOrVolume))
            .add(KSymbol('?'))

        // Should throw ambiguity exception
        assertThrows<Exception> {
            query.bestMatch(KTokenAssembly("How many cups are in a gallon ?"))
        }
    }

    // --------------------------------------------------------
    // Assembly Tests
    // --------------------------------------------------------

    /**
     * Show token assembly vs character assembly.
     *
     * Original: ShowAssemblies.java
     */
    @Test
    fun `show token assembly`() {
        val words = KRepetition<KToken>(subParser = KWord())
        val result = words.bestMatch(KTokenAssembly("hello world"))

        assertNotNull(result)
        assertEquals(2, result.getStack().size) // Two words
    }

    /**
     * Show character assembly.
     */
    @Test
    fun `show character assembly`() {
        val letters = KRepetition<Char>(subParser = KLetter())
        val result = letters.bestMatch(KCharAssembly("hello"))

        assertNotNull(result)
        assertEquals(5, result.getStack().size) // Five letters
    }

    // --------------------------------------------------------
    // Vacation Example (Verbose Repetition)
    // --------------------------------------------------------

    /**
     * Show vacation grammar parsing.
     *
     * Original: ShowVacation.java
     */
    @Test
    fun `show vacation grammar`() {
        val prepare = KAlternation<KToken>()
            .add(KLiteral("plan").discard())
            .add(KLiteral("shop").discard())
            .add(KLiteral("pack").discard())

        val enjoy = KAlternation<KToken>()
            .add(KLiteral("swim").discard())
            .add(KLiteral("hike").discard())
            .add(KLiteral("relax").discard())

        val vacation = KSequence<KToken>()
            .add(KRepetition(subParser = prepare))
            .add(KRepetition(subParser = enjoy))

        val result = vacation.bestMatch(KTokenAssembly("plan pack hike relax"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    // --------------------------------------------------------
    // Zero Match
    // --------------------------------------------------------

    /**
     * Show repetition can match zero times.
     *
     * Original: ShowZeroMatch.java
     */
    @Test
    fun `show zero match`() {
        val rep = KRepetition<KToken>(subParser = KLiteral("hello"))

        // Repetition can match zero times
        val result = rep.bestMatch(KTokenAssembly("world"))

        assertNotNull(result)
        // Zero matches means nothing consumed, but still valid
        assertEquals(1, result.remainItemNr())
    }

    // --------------------------------------------------------
    // A* AB Grammar
    // --------------------------------------------------------

    /**
     * Show A* AB grammar.
     *
     * Original: ShowAstarAB.java
     */
    @Test
    fun `show a star ab`() {
        // Grammar: A* A B
        val aStar = KRepetition<KToken>(subParser = KLiteral("a"))
        val ab = KSequence<KToken>()
            .add(aStar)
            .add(KLiteral("a"))
            .add(KLiteral("b"))

        val result = ab.bestMatch(KTokenAssembly("a a a b"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }
}
