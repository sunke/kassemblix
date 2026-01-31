package net.codenest.kassemblix.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KParserTest {

    private lateinit var parser: KParser<String>
    private lateinit var assembly: KAssembly<String>

    @BeforeEach
    fun setUp() {
        parser = object : KParser<String>("TestParser") {
            override fun match(assemblies: List<KAssembly<String>>): List<KAssembly<String>> {
                // Simple match implementation for testing
                return assemblies.map { it.apply { nextItem() } }
            }
        }
        assembly = KAssembly(" ")
    }

    @Test
    fun `test completeMatch returns null for incomplete match`() {
        assembly.addItem("item1").addItem("item2")
        val result = parser.completeMatch(assembly)
        assertNull(result)
    }

    @Test
    fun `test completeMatch returns assembly for complete match`() {
        assembly.addItem("item1")
        val result = parser.completeMatch(assembly)
        assertNotNull(result)
        assertFalse(result!!.hasMoreItem())
    }

    @Test
    fun `test bestMatch returns assembly with least unrecognized items`() {
        assembly.addItem("item1").addItem("item2")
        val result = parser.bestMatch(assembly)
        assertNotNull(result)
        assertEquals(1, result!!.remainItemNr())
    }

    /**
     * Tests that bestMatch() detects and rejects ambiguous grammars.
     *
     * Ambiguity occurs when a parser produces multiple complete matches for the same input.
     * In real grammars, this might happen with expressions like "1+2+3" that could be parsed
     * as either "(1+2)+3" or "1+(2+3)".
     *
     * This test uses a synthetic parser that always returns two copies of each assembly,
     * simulating a grammar that produces two equally valid complete parses.
     *
     * Setup:
     * - Empty assembly (remainItemNr = 0, meaning "nothing left to parse")
     * - Parser that duplicates each assembly without consuming anything
     *
     * Flow:
     * 1. bestMatch() calls match() with [emptyAssembly]
     * 2. match() returns [emptyAssembly, emptyAssembly.clone()] - two "complete" matches
     * 3. Both have remainItemNr = 0 (fully consumed)
     * 4. Ambiguity detected → Exception thrown
     *
     * The key condition in bestMatch():
     *   if (ays.size >= 2 && ays[0].remainItemNr() == 0 && ays[1].remainItemNr() == 0)
     *       throw Exception("Ambiguity detected")
     */
    @Test
    fun `test bestMatch throws exception for ambiguous grammar`() {
        // Parser that returns two copies of each input assembly (simulates ambiguous grammar)
        val ambiguousParser = object : KParser<String>("AmbiguousParser") {
            override fun match(assemblies: List<KAssembly<String>>): List<KAssembly<String>> {
                return assemblies.flatMap { listOf(it, it.clone()) }
            }
        }

        // assembly has no items, so remainItemNr = 0 (already "complete")
        // When duplicated, both copies have remainItemNr = 0 → ambiguity
        assertThrows(Exception::class.java) {
            ambiguousParser.bestMatch(assembly)
        }
    }

    @Test
    fun `test matchAndAssemble applies assembler`() {
        val assembler = object : KAssembler<String>() {
            override fun workOn(assembly: KAssembly<String>) {
                assembly.push("assembled")
            }
        }
        parser.setAssembler(assembler)
        assembly.addItem("item1")
        val result = parser.matchAndAssemble(listOf(assembly))
        assertEquals("assembled", result[0].pop())
    }
}