package net.codenest.kassemblix.examples.pretty

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Pretty printing examples demonstrating parse tree building.
 *
 * Converted from: com.sjm.examples.pretty.*
 *
 * These examples demonstrate:
 * - The Composite pattern for building parse trees
 * - Pretty printing of parsed structures
 * - Visualizing parse ambiguity
 *
 * Note: The original Java implementation uses a ParserVisitor pattern
 * to automatically attach assemblers to all parsers. This Kotlin version
 * demonstrates the concepts with manual assembler attachment.
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Parse Tree Node Classes (Composite Pattern)
// ============================================================

/**
 * Abstract base class for parse tree nodes.
 *
 * Original: ComponentNode.java
 */
sealed class ComponentNode {
    /**
     * Returns a string of blanks for indentation.
     */
    protected fun indent(n: Int) = "    ".repeat(n)

    /**
     * Returns a textual description of this node.
     */
    override fun toString() = toString(0, true, mutableSetOf())

    /**
     * Returns a textual description without showing composite labels.
     */
    fun toStringWithoutLabels() = toString(0, false, mutableSetOf())

    /**
     * Returns a textual description with the given indentation level.
     */
    internal abstract fun toString(depth: Int, showLabels: Boolean, visited: MutableSet<ComponentNode>): String
}

/**
 * A composite node that contains other nodes.
 *
 * Original: CompositeNode.java
 */
class CompositeNode(private val value: Any) : ComponentNode() {
    private val children = mutableListOf<ComponentNode>()

    fun add(node: ComponentNode) = apply { children.add(node) }

    fun insert(node: ComponentNode) = apply { children.add(0, node) }

    internal override fun toString(depth: Int, showLabels: Boolean, visited: MutableSet<ComponentNode>): String {
        if (this in visited) return "...\n"
        visited.add(this)

        val buf = StringBuilder()
        if (showLabels) {
            buf.append(indent(depth))
            buf.append(value)
            buf.append("\n")
        }
        for (child in children) {
            buf.append(child.toString(depth + 1, showLabels, visited))
        }
        return buf.toString()
    }
}

/**
 * A terminal node that holds a leaf value.
 *
 * Original: TerminalNode.java
 */
class TerminalNode(private val value: Any) : ComponentNode() {
    internal override fun toString(depth: Int, showLabels: Boolean, visited: MutableSet<ComponentNode>): String {
        return indent(depth) + value + "\n"
    }
}

// ============================================================
// Pretty Assemblers
// ============================================================

/**
 * Replaces a token on the stack with a TerminalNode.
 *
 * Original: PrettyTerminalAssembler.java
 */
class PrettyTerminalAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val t = assembly.pop() as KToken
        assembly.push(TerminalNode(t.value() ?: ""))
    }
}

/**
 * Creates a CompositeNode from a sequence of nodes.
 *
 * Original: PrettySequenceAssembler.java
 */
class PrettySequenceAssembler(private val name: String, private val nodeCount: Int) : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val composite = CompositeNode(name)
        val nodes = mutableListOf<ComponentNode>()

        repeat(nodeCount) {
            val item = assembly.pop()
            if (item is ComponentNode) {
                nodes.add(item)
            }
        }

        nodes.reversed().forEach { composite.add(it) }
        assembly.push(composite)
    }
}

/**
 * Creates a CompositeNode for an alternation choice.
 *
 * Original: PrettyAlternationAssembler.java
 */
class PrettyAlternationAssembler(private val name: String) : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val item = assembly.pop() ?: return
        if (item is ComponentNode) {
            val composite = CompositeNode(name)
            composite.add(item)
            assembly.push(composite)
        } else {
            assembly.push(item)
        }
    }
}

// ============================================================
// Example Grammars with Pretty Printing
// ============================================================

/**
 * A simple expression grammar with pretty printing.
 *
 * Grammar:
 * ```
 *     expr = term ('+' term)*;
 *     term = Num;
 * ```
 */
class PrettyExprParser {
    fun expr(): KParser<KToken> {
        val num = KNum().setAssembler(PrettyTerminalAssembler())

        val plusTerm = KSequence<KToken>()
            .add(KSymbol('+').setAssembler(PrettyTerminalAssembler()))
            .add(num)

        plusTerm.setAssembler(PrettySequenceAssembler("plusTerm", 2))

        return KSequence<KToken>()
            .add(num)
            .add(KRepetition(subParser = plusTerm))
    }
}

/**
 * A simple statement grammar for demonstrating pretty printing.
 *
 * Grammar:
 * ```
 *     statement = ifStatement | assignment;
 *     ifStatement = "if" "(" condition ")" statement;
 *     assignment = Word "=" Num ";";
 *     condition = Word "<" Num;
 * ```
 */
class PrettyStatementParser {
    private var statement: KAlternation<KToken>? = null

    fun statement(): KParser<KToken> {
        if (statement == null) {
            statement = KAlternation("<statement>")
            statement!!.add(ifStatement())
            statement!!.add(assignment())
        }
        return statement!!
    }

    private fun ifStatement(): KParser<KToken> {
        return KSequence<KToken>("<if>")
            .add(KLiteral("if").setAssembler(PrettyTerminalAssembler()))
            .add(KSymbol('(').setAssembler(PrettyTerminalAssembler()))
            .add(condition())
            .add(KSymbol(')').setAssembler(PrettyTerminalAssembler()))
            .add(statement())
            .also { it.setAssembler(PrettySequenceAssembler("<if>", 5)) }
    }

    private fun assignment(): KParser<KToken> {
        return KSequence<KToken>("<assignment>")
            .add(KWord().setAssembler(PrettyTerminalAssembler()))
            .add(KSymbol('=').setAssembler(PrettyTerminalAssembler()))
            .add(KNum().setAssembler(PrettyTerminalAssembler()))
            .add(KSymbol(';').setAssembler(PrettyTerminalAssembler()))
            .also { it.setAssembler(PrettySequenceAssembler("<assignment>", 4)) }
    }

    private fun condition(): KParser<KToken> {
        return KSequence<KToken>("<condition>")
            .add(KWord().setAssembler(PrettyTerminalAssembler()))
            .add(KSymbol('<').setAssembler(PrettyTerminalAssembler()))
            .add(KNum().setAssembler(PrettyTerminalAssembler()))
            .also { it.setAssembler(PrettySequenceAssembler("<condition>", 3)) }
    }
}

// ============================================================
// Test class
// ============================================================

class PrettyTest {

    /**
     * Show basic terminal node.
     */
    @Test
    fun `show terminal node`() {
        val node = TerminalNode("hello")
        assertEquals("hello\n", node.toString())
    }

    /**
     * Show composite node with children.
     */
    @Test
    fun `show composite node`() {
        val composite = CompositeNode("parent")
        composite.add(TerminalNode("child1"))
        composite.add(TerminalNode("child2"))

        val result = composite.toString()
        assertTrue(result.contains("parent"))
        assertTrue(result.contains("child1"))
        assertTrue(result.contains("child2"))
    }

    /**
     * Show nested composite nodes.
     */
    @Test
    fun `show nested composites`() {
        val root = CompositeNode("root")
        val child = CompositeNode("child")
        child.add(TerminalNode("leaf"))
        root.add(child)

        val result = root.toString()
        assertTrue(result.contains("root"))
        assertTrue(result.contains("child"))
        assertTrue(result.contains("leaf"))
    }

    /**
     * Show composite without labels.
     */
    @Test
    fun `show composite without labels`() {
        val composite = CompositeNode("hidden")
        composite.add(TerminalNode("visible"))

        val result = composite.toStringWithoutLabels()
        assertTrue(!result.contains("hidden"))
        assertTrue(result.contains("visible"))
    }

    /**
     * Show pretty terminal assembler.
     */
    @Test
    fun `show pretty terminal assembler`() {
        val parser = KNum().setAssembler(PrettyTerminalAssembler())
        val result = parser.bestMatch(KTokenAssembly("42"))

        assertNotNull(result)
        val node = result.pop()
        assertTrue(node is TerminalNode)
    }

    /**
     * Show pretty sequence assembler.
     */
    @Test
    fun `show pretty sequence assembler`() {
        val parser = KSequence<KToken>()
            .add(KWord().setAssembler(PrettyTerminalAssembler()))
            .add(KNum().setAssembler(PrettyTerminalAssembler()))

        parser.setAssembler(PrettySequenceAssembler("pair", 2))

        val result = parser.bestMatch(KTokenAssembly("hello 42"))

        assertNotNull(result)
        val node = result.pop()
        assertTrue(node is CompositeNode)

        val str = node.toString()
        assertTrue(str.contains("pair"))
        assertTrue(str.contains("hello"))
        assertTrue(str.contains("42"))
    }

    /**
     * Show pretty expression parsing.
     */
    @Test
    fun `show pretty expression`() {
        val parser = PrettyExprParser().expr()
        val result = parser.bestMatch(KTokenAssembly("1 + 2 + 3"))

        assertNotNull(result)
        // Just verify it parses without error
    }

    /**
     * Show pretty statement parsing.
     */
    @Test
    fun `show pretty statement`() {
        val parser = PrettyStatementParser().statement()
        val result = parser.bestMatch(KTokenAssembly("x = 5 ;"))

        assertNotNull(result)
        val node = result.pop()
        assertTrue(node is CompositeNode)

        val str = node.toString()
        assertTrue(str.contains("x"))
        assertTrue(str.contains("5"))
    }

    /**
     * Show pretty nested if statement.
     */
    @Test
    fun `show pretty nested if`() {
        val parser = PrettyStatementParser().statement()
        val result = parser.bestMatch(KTokenAssembly("if ( x < 5 ) y = 10 ;"))

        assertNotNull(result)
        val node = result.pop()
        assertTrue(node is CompositeNode)

        val str = node.toString()
        assertTrue(str.contains("if"))
        assertTrue(str.contains("x"))
        assertTrue(str.contains("y"))
    }

    /**
     * Show cycle detection in composite toString.
     */
    @Test
    fun `show cycle detection`() {
        val node1 = CompositeNode("node1")
        val node2 = CompositeNode("node2")
        node1.add(node2)
        node2.add(node1) // Create cycle

        // Should not infinite loop
        val str = node1.toString()
        assertTrue(str.contains("...")) // Cycle marker
    }

    /**
     * Note: The original Java implementation uses a ParserVisitor to
     * automatically attach pretty assemblers to all parsers in a composite.
     * This allows any parser to be "pretty printed" without modification.
     *
     * The Kotlin version demonstrates the concepts with manual assembler
     * attachment. A full implementation would require adding:
     * - A visitor pattern to KParser classes
     * - getName() and getSubparsers() methods to KParser
     * - Pre-assemblers and post-assemblers for repetitions
     *
     * The ShowDangle example from Java shows how an ambiguous parser
     * produces multiple parse trees, which can be visualized with pretty
     * printing to understand the ambiguity.
     */
}
