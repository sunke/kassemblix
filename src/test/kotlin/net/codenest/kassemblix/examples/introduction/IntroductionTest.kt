package net.codenest.kassemblix.examples.introduction

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Introduction examples demonstrating basic parser concepts.
 */
class IntroductionTest {

    /**
     * Show how a TokenAssembly divides up a string.
     */
    @Test
    fun `show token assembly divides string into tokens`() {
        val s = "int i = 3;"
        val assembly = KTokenAssembly(s)

        val tokens = mutableListOf<String>()
        while (assembly.hasMoreItem()) {
            tokens.add(assembly.nextItem().toString())
        }

        assertEquals(listOf("int", "i", "=", "3.0", ";"), tokens)
    }

    /**
     * Show how to recognize terminals in a string.
     */
    @Test
    fun `show terminal recognizes words`() {
        val s = "steaming hot coffee"
        var assembly: KAssembly<KToken>? = KTokenAssembly(s)
        val parser = KWord()

        val results = mutableListOf<String>()
        while (assembly != null) {
            results.add(assembly.toString())
            assembly = parser.bestMatch(assembly)
        }

        // Each step shows progress: stack grows, consumed items increase
        assertEquals(4, results.size)
        assertEquals("[steaming]|steaming^hot/coffee", results[1])
        assertEquals("[steaming, hot]|steaming/hot^coffee", results[2])
        assertEquals("[steaming, hot, coffee]|steaming/hot/coffee^", results[3])
    }

    /**
     * Show a parser that recognizes an int declaration.
     */
    @Test
    fun `show literal recognizes int declaration`() {
        val parser = KSequence<KToken>()
            .add(KLiteral("int"))
            .add(KWord())
            .add(KSymbol("="))
            .add(KNum())
            .add(KSymbol(";"))

        val assembly = parser.completeMatch(KTokenAssembly("int i = 3;"))

        assertNotNull(assembly)
        assertEquals(listOf("int", "i", "=", 3.0, ";"), assembly.getStack().map {
            when (it) {
                is KToken -> if (it.isNumber()) it.nval else it.sval
                else -> it
            }
        })
    }

    /**
     * Show what counts as a number.
     *
     * Original: ShowNums.java
     *
     * Note: The Kotlin tokenizer doesn't support leading dots (e.g., .1234).
     * Such input is tokenized as "." + "1234" instead of "0.1234".
     */
    @Test
    fun `show nums tokenizes various number formats`() {
        // Test standard number formats that KTokenizer supports
        val s = "12 12.34 0.1234 1234e-2"
        val assembly = KTokenAssembly(s)

        val numbers = mutableListOf<Double>()
        while (assembly.hasMoreItem()) {
            val token = assembly.nextItem()!!
            if (token.isNumber()) {
                numbers.add(token.nval)
            }
        }

        assertEquals(4, numbers.size)
        assertEquals(12.0, numbers[0])
        assertEquals(12.34, numbers[1])
        assertEquals(0.1234, numbers[2], 0.0001)
        assertEquals(12.34, numbers[3], 0.0001) // 1234e-2 = 12.34
    }

    /**
     * Show that a Repetition object creates multiple interpretations.
     *
     * Original: ShowRepetition.java
     */
    @Test
    fun `show repetition creates multiple interpretations`() {
        val s = "steaming hot coffee"
        val assembly = KTokenAssembly(s)
        val parser = KRepetition(subParser = KWord())

        val results = parser.match(listOf(assembly))

        // Repetition returns all possible matches: 0, 1, 2, or 3 words
        assertEquals(4, results.size)
    }

    /**
     * Show how to create a composite parser.
     *
     * Original: ShowComposite.java
     */
    @Test
    fun `show composite parser with alternation and sequence`() {
        val adjective = KAlternation<KToken>()
            .add(KLiteral("steaming"))
            .add(KLiteral("hot"))

        val good = KSequence<KToken>()
            .add(KRepetition(subParser = adjective))
            .add(KLiteral("coffee"))

        val s = "hot hot steaming hot coffee"
        val assembly = KTokenAssembly(s)
        val result = good.bestMatch(assembly)

        assertNotNull(result)
        // Stack should contain all matched tokens
        assertEquals(5, result.getStack().size)
    }

    /**
     * Show how to put the Empty class to good use.
     *
     * A list, in this example, is a pair of brackets around some contents.
     * The contents may be empty, or may be an actual list.
     *
     * Original: ShowEmpty.java
     */
    @Test
    fun `show empty parser for optional content`() {
        val empty = KEmpty<KToken>()

        val commaTerm = KSequence<KToken>()
            .add(KSymbol(",").discard())
            .add(KWord())

        val actualList = KSequence<KToken>()
            .add(KWord())
            .add(KRepetition(subParser = commaTerm))

        val contents = KAlternation<KToken>()
            .add(empty)
            .add(actualList)

        val list = KSequence<KToken>()
            .add(KSymbol("[").discard())
            .add(contents)
            .add(KSymbol("]").discard())

        // Test with full list
        val result1 = list.completeMatch(KTokenAssembly("[die_bonder_2, oven_7, wire_bonder_3, mold_1]"))
        assertNotNull(result1)
        assertEquals(4, result1.getStack().size)

        // Test with empty list
        val result2 = list.completeMatch(KTokenAssembly("[]"))
        assertNotNull(result2)
        assertEquals(0, result2.getStack().size)

        // Test with single item
        val result3 = list.completeMatch(KTokenAssembly("[mold_1]"))
        assertNotNull(result3)
        assertEquals(1, result3.getStack().size)
    }

    /**
     * Show how to recognize a quoted string.
     *
     * Original: ShowQuotedString.java
     */
    @Test
    fun `show quoted string recognition`() {
        val parser = KQuotedString()
        val id = "\"Clark Kent\""
        val result = parser.bestMatch(KTokenAssembly(id))

        assertNotNull(result)
        val stack = result.getStack()
        assertEquals(1, stack.size)
        assertEquals("\"Clark Kent\"", (stack[0] as KToken).sval)
    }

    /**
     * Show that apostrophes can be parts of words and can contain quoted strings.
     *
     * Original: ShowApostrophe.java
     */
    @Test
    fun `show apostrophe handling in tokens`() {
        val s = "Let's 'rock and roll'!"
        val assembly = KTokenAssembly(s)

        val tokens = mutableListOf<String>()
        while (assembly.hasMoreItem()) {
            tokens.add(assembly.nextItem().toString())
        }

        // "Let's" is one word, 'rock and roll' is a quoted string
        assertEquals(3, tokens.size)
        assertEquals("Let's", tokens[0])
        assertEquals("'rock and roll'", tokens[1])
        assertEquals("!", tokens[2])
    }

    /**
     * Show how an assembly prints itself.
     *
     * Original: ShowAssemblyAppearance.java
     */
    @Test
    fun `show assembly appearance and string representation`() {
        val s1 = "Congress admitted Colorado in 1876."
        val a1 = KTokenAssembly(s1)
        // Format: [stack]|consumed^remaining
        assertEquals("[]|^Congress/admitted/Colorado/in/1876.0/.", a1.toString())

        val s2 = "admitted(colorado, 1876)"
        val a2 = KTokenAssembly(s2)
        assertEquals("[]|^admitted/(/colorado/,/1876.0/)", a2.toString())
    }

    /**
     * Show how to use Assembler.elementsAbove().
     *
     * Original: ShowElementsAbove.java
     */
    @Test
    fun `show elements above fence marker`() {
        var capturedElements: List<Any>? = null

        val list = KSequence<KToken>()
            .add(KSymbol("{"))
            .add(KRepetition(subParser = KWord()))
            .add(KSymbol("}").discard())

        list.setAssembler(object : KTokenAssembler() {
            override fun workOn(assembly: KAssembly<KToken>) {
                val fence = KToken.createSymbol("{")
                capturedElements = elementsAbove(assembly, fence)
            }
        })

        list.bestMatch(KTokenAssembly("{ Washington Adams Jefferson }"))

        assertNotNull(capturedElements)
        // Elements are popped in reverse order (LIFO)
        val names = capturedElements.map { (it as KToken).sval }
        assertEquals(listOf("Jefferson", "Adams", "Washington"), names)
    }
}
