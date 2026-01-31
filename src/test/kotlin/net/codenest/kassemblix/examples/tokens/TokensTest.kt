package net.codenest.kassemblix.examples.tokens

import net.codenest.kassemblix.lexer.*
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Token handling examples demonstrating tokenizer features.
 *
 * Converted from: com.sjm.examples.tokens.*
 *
 * @author Steven J. Metsker
 */
class TokensTest {

    /**
     * Show some aspects of default tokenization.
     *
     * Original: ShowDefaultTokenization.java
     */
    @Test
    fun `show default tokenization`() {
        val tokenizer = KTokenizer(StringReader(">give 2receive"))

        val tokens = mutableListOf<KToken>()
        var token = tokenizer.nextToken()
        while (token != KToken.END) {
            if (token != KToken.SKIP) {
                tokens.add(token)
            }
            token = tokenizer.nextToken()
        }

        assertEquals(4, tokens.size)
        assertEquals(">", tokens[0].sval)
        assertTrue(tokens[0].isSymbol())
        assertEquals("give", tokens[1].sval)
        assertTrue(tokens[1].isWord())
        assertEquals(2.0, tokens[2].nval)
        assertTrue(tokens[2].isNumber())
        assertEquals("receive", tokens[3].sval)
        assertTrue(tokens[3].isWord())
    }

    /**
     * Show a Tokenizer consuming words.
     *
     * Original: ShowWord.java
     */
    @Test
    fun `show word tokenization`() {
        val tokenizer = KTokenizer(StringReader("A65 B66"))

        val tokens = mutableListOf<KToken>()
        var token = tokenizer.nextToken()
        while (token != KToken.END) {
            if (token != KToken.SKIP) {
                tokens.add(token)
            }
            token = tokenizer.nextToken()
        }

        // "A65" and "B66" are words (start with letters)
        assertEquals(2, tokens.size)
        assertEquals("A65", tokens[0].sval)
        assertTrue(tokens[0].isWord())
        assertEquals("B66", tokens[1].sval)
        assertTrue(tokens[1].isWord())
    }

    /**
     * Show a default Tokenizer at work with complex input including
     * comments and various token types.
     *
     * Original: ShowTokenizer.java
     */
    @Test
    fun `show tokenizer with complex input`() {
        val s = "\"It's 123 blast-off!\", she said, // watch out!\n" +
                "and <= 3 'ticks' later /* wince */ , it's blast-off!"

        val tokenizer = KTokenizer(StringReader(s))

        val tokens = mutableListOf<KToken>()
        var token = tokenizer.nextToken()
        while (token != KToken.END) {
            if (token != KToken.SKIP) {
                tokens.add(token)
            }
            token = tokenizer.nextToken()
        }

        // Verify some key tokens are present
        assertTrue(tokens.any { it.isQuotedString() && it.sval == "\"It's 123 blast-off!\"" })
        assertTrue(tokens.any { it.isWord() && it.sval == "she" })
        assertTrue(tokens.any { it.isWord() && it.sval == "said" })
        assertTrue(tokens.any { it.isSymbol() && it.sval == "<=" })
        assertTrue(tokens.any { it.isNumber() && it.nval == 3.0 })
        assertTrue(tokens.any { it.isQuotedString() && it.sval == "'ticks'" })
        // Comments (// and /* */) should be skipped
    }

    /**
     * Show how to add a new multi-character symbol.
     *
     * Original: ShowNewSymbol.java
     */
    @Test
    fun `show new multi-character symbol`() {
        // Add custom symbol before tokenizing
        KSymbolState.addSymbol("=~=")

        val tokenizer = KTokenizer(StringReader("42.001 =~= 42"))

        val tokens = mutableListOf<KToken>()
        var token = tokenizer.nextToken()
        while (token != KToken.END) {
            if (token != KToken.SKIP) {
                tokens.add(token)
            }
            token = tokenizer.nextToken()
        }

        assertEquals(3, tokens.size)
        assertEquals(42.001, tokens[0].nval)
        assertEquals("=~=", tokens[1].sval)  // Multi-char symbol
        assertEquals(42.0, tokens[2].nval)
    }

    /**
     * Show how to NOT ignore Java-style comments by changing slash handling.
     *
     * Original: ShowNoComment.java
     *
     * Note: In KTokenizer, we can change the state table to treat '/' as a symbol
     * instead of triggering comment handling.
     */
    @Test
    fun `show no comment handling`() {
        // Save original state and change '/' to symbol state
        KTokenizerStateTable.setState('/', KSymbolState)

        try {
            val tokenizer = KTokenizer(StringReader("Show /* all */ // this"))

            val tokens = mutableListOf<KToken>()
            var token = tokenizer.nextToken()
            while (token != KToken.END) {
                if (token != KToken.SKIP) {
                    tokens.add(token)
                }
                token = tokenizer.nextToken()
            }

            // Now /* and // are not treated as comments
            assertTrue(tokens.any { it.sval == "/" })
            assertTrue(tokens.any { it.sval == "*" })
            assertTrue(tokens.any { it.sval == "all" })
            assertTrue(tokens.any { it.sval == "this" })
        } finally {
            // Restore '/' to slash state
            KTokenizerStateTable.setState('/', KSlashState)
        }
    }

    /**
     * Show how QuoteState works with custom quote characters.
     *
     * Original: ShowQuoteState.java
     *
     * Note: KTokenizer uses '#' as a symbol by default. We can change it to
     * use quote state for custom quote characters.
     *
     * Note: KQuoteState throws an exception for unclosed quotes, so we use
     * properly closed quotes in this test.
     */
    @Test
    fun `show quote state with custom character`() {
        // Change '#' to be handled by quote state
        KTokenizerStateTable.setState('#', KQuoteState)

        try {
            val tokenizer = KTokenizer(StringReader(
                "Hamlet says #Alas, poor Yorick!# and #To be or not to be#"
            ))

            val tokens = mutableListOf<KToken>()
            var token = tokenizer.nextToken()
            while (token != KToken.END) {
                if (token != KToken.SKIP) {
                    tokens.add(token)
                }
                token = tokenizer.nextToken()
            }

            // "#Alas, poor Yorick!#" should be a quoted string
            assertTrue(tokens.any { it.isQuotedString() && it.sval?.contains("Alas") == true })
            // "#To be or not to be#" should also be a quoted string
            assertTrue(tokens.any { it.isQuotedString() && it.sval?.contains("To be") == true })
        } finally {
            // Restore '#' to symbol state
            KTokenizerStateTable.setState('#', KSymbolState)
        }
    }

    /**
     * Show collaboration of token-related objects: Tokenizer, TokenStringSource,
     * TokenString, and TokenAssembly.
     *
     * Original: ShowTokenString.java
     */
    @Test
    fun `show token string source`() {
        // A parser that counts words
        val wordCounter = KWord().discard()
        wordCounter.setAssembler(object : KTokenAssembler() {
            override fun workOn(assembly: KAssembly<KToken>) {
                if (assembly.stackIsEmpty()) {
                    assembly.push(1)
                } else {
                    val count = assembly.pop() as Int
                    assembly.push(count + 1)
                }
            }
        })

        val parser = KRepetition<KToken>(subParser = wordCounter)

        // Consume token strings separated by semicolons
        val s = "I came; I saw; I left in peace;"
        val tokenizer = KTokenizer(StringReader(s))
        val source = KTokenStringSource(tokenizer, ";")

        val results = mutableListOf<Pair<String, Int>>()
        while (source.hasMoreTokenStrings()) {
            val tokenString = source.nextTokenString()!!
            // Create assembly from token string
            val assembly = KAssembly<KToken>()
            tokenString.tokens.forEach { assembly.addItem(it) }

            val result = parser.completeMatch(assembly)
            assertNotNull(result)
            val wordCount = result.pop() as Int
            results.add(tokenString.toString() to wordCount)
        }

        assertEquals(3, results.size)
        assertEquals(2, results[0].second)  // "I came" = 2 words
        assertEquals(2, results[1].second)  // "I saw" = 2 words
        assertEquals(4, results[2].second)  // "I left in peace" = 4 words
    }

    /**
     * Show that KTokenizer already supports scientific notation.
     *
     * Original: ShowScientific.java (simplified - without ArithmeticParser)
     *
     * Note: The Kotlin KNumberState already handles scientific notation like 1e2.
     */
    @Test
    fun `show scientific notation support`() {
        val tokenizer = KTokenizer(StringReader("1e2 1e1 1e0 1e-1 1e-2"))

        val numbers = mutableListOf<Double>()
        var token = tokenizer.nextToken()
        while (token != KToken.END) {
            if (token.isNumber()) {
                numbers.add(token.nval)
            }
            token = tokenizer.nextToken()
        }

        assertEquals(5, numbers.size)
        assertEquals(100.0, numbers[0], 0.001)   // 1e2 = 100
        assertEquals(10.0, numbers[1], 0.001)    // 1e1 = 10
        assertEquals(1.0, numbers[2], 0.001)     // 1e0 = 1
        assertEquals(0.1, numbers[3], 0.001)     // 1e-1 = 0.1
        assertEquals(0.01, numbers[4], 0.001)    // 1e-2 = 0.01
    }

    /**
     * Note: ShowTokenizer2.java demonstrates Java's StreamTokenizer for comparison.
     * This is Java-specific and not converted to Kotlin. The purpose was to show
     * differences between SJM's Tokenizer and Java's built-in StreamTokenizer.
     *
     * Note: ShowSuppliedReader.java shows file I/O with tokenizer.
     * Skipped as it requires file system operations and KTokenizer
     * already accepts a Reader in its constructor.
     *
     * Note: ScientificNumberState.java is a custom TokenizerState.
     * KNumberState already supports scientific notation, so this is not needed.
     */
}
