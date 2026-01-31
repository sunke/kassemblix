package net.codenest.kassemblix.examples.preface

import net.codenest.kassemblix.parser.KAnyToken
import net.codenest.kassemblix.parser.KRepetition
import net.codenest.kassemblix.parser.KTokenAssembly
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * This is the simplest demo of "Hello world"
 */
class ShowHelloTest {

    /**
     * Create a little parser and use it to recognize "Hello world!".
     *
     * The parser uses:
     * - KAnyToken: matches any token
     * - KRepetition: matches zero or more occurrences
     *
     * Input: "Hello world!"
     * Expected output: Stack contains [Hello, world, !]
     */
    @Test
    fun `show hello world parsing`() {
        val terminal = KAnyToken()
        val repetition = KRepetition(subParser = terminal)

        val input = KTokenAssembly("Hello world!")
        val output = repetition.completeMatch(input)

        assertNotNull(output, "Parser should successfully match the input")

        val stack = output.getStack()
        assertEquals(3, stack.size, "Stack should contain 3 tokens")

        // The stack contains the tokens that were pushed during parsing
        // Each token's string representation is its value
        val stackStrings = stack.map { it.toString() }
        assertEquals(listOf("Hello", "world", "!"), stackStrings)
    }
}
