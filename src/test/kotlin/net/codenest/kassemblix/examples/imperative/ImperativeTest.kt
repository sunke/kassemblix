package net.codenest.kassemblix.examples.imperative

import net.codenest.kassemblix.examples.engine.*
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.assertEquals

/**
 * Imperative command examples demonstrating the command pattern.
 *
 * Converted from: com.sjm.examples.imperative.*
 *
 * These examples demonstrate:
 * - Command pattern implementation
 * - For loops with assignment commands
 * - Print commands
 *
 * @author Steven J. Metsker
 */
class ImperativeTest {

    /**
     * Show a simple composition of commands.
     *
     * Original: ShowCommand.java
     *
     * Demonstrates a for loop that prints "go!" five times.
     */
    @Test
    fun `show command`() {
        val output = StringWriter()
        val writer = PrintWriter(output)

        val go = Fact("go!")
        val p = PrintlnCommand(go, writer)

        val i = Variable("i")
        val f = ForCommand(i, 1, 5, p)

        f.execute()

        val result = output.toString()
        // Should print "go!" five times
        val lines = result.trim().split("\n")
        assertEquals(5, lines.size)
        lines.forEach { assertEquals("go!", it) }
    }

    /**
     * Show the assignment command.
     *
     * Original: ShowAssignmentCommand.java
     *
     * Creates a variable "x" and pre-assigns it 0. Then executes:
     * ```
     *     for (int i = 1; i <= 4; i++) {
     *         x = x * 10 + 1;
     *     }
     * ```
     * This leaves x with the value 1111.0.
     */
    @Test
    fun `show assignment command`() {
        val x = Variable("x")
        x.unify(NumberFact(0.0))

        // *(x, 10.0)
        val op1 = ArithmeticOperator('*', x, NumberFact(10.0))

        // +(*(x, 10.0), 1.0)
        val op2 = ArithmeticOperator('+', op1, NumberFact(1.0))

        // #(x, +(*(x, 10.0), 1.0))
        val ac = AssignmentCommand(Evaluation(x, op2))

        val f = ForCommand(Variable("i"), 1, 4, ac)

        f.execute()
        assertEquals(1111.0, x.eval())
    }

    /**
     * Show if command.
     */
    @Test
    fun `show if command`() {
        val output = StringWriter()
        val writer = PrintWriter(output)

        val x = Variable("x")
        x.unify(NumberFact(10.0))

        // if (x > 5) print "big" else print "small"
        val condition = Comparison(">", x, NumberFact(5.0))
        val ifCmd = PrintlnCommand(Fact("big"), writer)
        val elseCmd = PrintlnCommand(Fact("small"), writer)
        val cmd = IfCommand(condition, ifCmd, elseCmd)

        cmd.execute()
        assertEquals("big\n", output.toString())
    }

    /**
     * Show while command.
     */
    @Test
    fun `show while command`() {
        val x = Variable("x")
        x.unify(NumberFact(0.0))

        // x = x + 1
        val increment = ArithmeticOperator('+', x, NumberFact(1.0))
        val assign = AssignmentCommand(Evaluation(x, increment))

        // while (x < 10) x = x + 1
        val condition = Comparison("<", x, NumberFact(10.0))
        val whileCmd = WhileCommand(condition, assign)

        whileCmd.execute()
        assertEquals(10.0, x.eval())
    }

    /**
     * Show command sequence.
     */
    @Test
    fun `show command sequence`() {
        val output = StringWriter()
        val writer = PrintWriter(output)

        val seq = CommandSequence()
        seq.addCommand(PrintlnCommand(Fact("one"), writer))
        seq.addCommand(PrintlnCommand(Fact("two"), writer))
        seq.addCommand(PrintlnCommand(Fact("three"), writer))

        seq.execute()

        val lines = output.toString().trim().split("\n")
        assertEquals(3, lines.size)
        assertEquals("one", lines[0])
        assertEquals("two", lines[1])
        assertEquals("three", lines[2])
    }

    /**
     * Show null command.
     */
    @Test
    fun `show null command`() {
        val nullCmd = NullCommand()
        nullCmd.execute() // Should do nothing without error
        assertEquals("NullCommand", nullCmd.toString())
    }

    /**
     * Show for command with step.
     */
    @Test
    fun `show for command with step`() {
        val sum = Variable("sum")
        sum.unify(NumberFact(0.0))

        val i = Variable("i")

        // sum = sum + i
        val addI = ArithmeticOperator('+', sum, i)
        val assign = AssignmentCommand(Evaluation(sum, addI))

        // for (i = 0; i <= 10; i += 2) sum = sum + i
        val f = ForCommand(i, 0.0, 10.0, 2.0, assign)

        f.execute()
        // 0 + 2 + 4 + 6 + 8 + 10 = 30
        assertEquals(30.0, sum.eval())
    }
}
