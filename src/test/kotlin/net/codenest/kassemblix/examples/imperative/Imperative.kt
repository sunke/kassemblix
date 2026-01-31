package net.codenest.kassemblix.examples.imperative

import net.codenest.kassemblix.examples.engine.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintWriter

/**
 * Imperative Commands - Command pattern implementation for imperative execution.
 *
 * Converted from: com.sjm.imperative.*
 *
 * This package provides:
 * - Command base class and common commands
 * - Control flow commands (if, while, for)
 * - I/O commands (print, read)
 * - Assignment commands
 *
 * @author Steven J. Metsker (original Java)
 */

// ============================================================
// Command Base Class
// ============================================================

/**
 * Abstract base class for commands.
 * A command object is a request that is dormant until a caller
 * asks it to execute.
 */
abstract class Command {
    /**
     * Perform the request encapsulated in this command.
     */
    abstract fun execute()
}

// ============================================================
// NullCommand
// ============================================================

/**
 * This command does nothing, which can simplify coding.
 * For example, an "if" command with no given "else" uses
 * a NullCommand for its else command.
 */
class NullCommand : Command() {
    override fun execute() {
        // Does nothing
    }

    override fun toString(): String = "NullCommand"
}

// ============================================================
// CommandSequence
// ============================================================

/**
 * This class contains a sequence of other commands.
 */
class CommandSequence : Command() {
    private var commands: MutableList<Command>? = null

    /**
     * Add a command to the sequence.
     */
    fun addCommand(c: Command) {
        getCommands().add(c)
    }

    private fun getCommands(): MutableList<Command> {
        if (commands == null) {
            commands = mutableListOf()
        }
        return commands!!
    }

    override fun execute() {
        for (cmd in getCommands()) {
            Thread.yield()
            cmd.execute()
        }
    }

    override fun toString(): String {
        return getCommands().joinToString("\n")
    }
}

// ============================================================
// AssignmentCommand
// ============================================================

/**
 * This class holds an Evaluation object and executes it
 * upon receiving an execute command.
 */
class AssignmentCommand(private val evaluation: Evaluation) : Command() {

    override fun execute() {
        // Note: we can only unbind the variable after
        // evaluating the first term. Not doing this
        // would create a defect with i := i + 1
        val o = evaluation.terms[1].eval()
        val v = evaluation.terms[0] as Variable
        v.unbind()
        v.unify(Atom(o!!))
    }

    override fun toString(): String = evaluation.toString()
}

// ============================================================
// PrintlnCommand
// ============================================================

/**
 * This command, when executed, prints out the value of
 * a term provided in the constructor.
 */
class PrintlnCommand(
    private val term: Term,
    private val out: PrintWriter = PrintWriter(System.out)
) : Command() {

    override fun execute() {
        out.print("${term.eval()}\n")
        out.flush()
    }

    override fun toString(): String = "println($term)"
}

// ============================================================
// ReadCommand
// ============================================================

/**
 * This command, when executed, reads in a string and
 * assigns it to a supplied variable.
 */
class ReadCommand : Command {
    private val variable: Variable
    private val reader: BufferedReader

    constructor(variable: Variable) : this(variable, System.`in`)

    constructor(variable: Variable, reader: BufferedReader) {
        this.variable = variable
        this.reader = reader
    }

    constructor(variable: Variable, input: InputStream) :
        this(variable, BufferedReader(InputStreamReader(input)))

    override fun execute() {
        val s = try {
            reader.readLine() ?: ""
        } catch (e: Exception) {
            ""
        }
        val e = Evaluation(variable, Atom(s))
        val ac = AssignmentCommand(e)
        ac.execute()
    }

    override fun toString(): String = "read(${variable.name})"
}

// ============================================================
// IfCommand
// ============================================================

/**
 * This command mimics a normal "if" statement.
 */
class IfCommand : Command {
    private val condition: BooleanTerm
    private val ifCommand: Command
    private val elseCommand: Command

    constructor(condition: BooleanTerm, ifCommand: Command) {
        this.condition = condition
        this.ifCommand = ifCommand
        this.elseCommand = NullCommand()
    }

    constructor(condition: BooleanTerm, ifCommand: Command, elseCommand: Command) {
        this.condition = condition
        this.ifCommand = ifCommand
        this.elseCommand = elseCommand
    }

    override fun execute() {
        val b = condition.eval() as Boolean
        if (b) {
            ifCommand.execute()
        } else {
            elseCommand.execute()
        }
    }

    override fun toString(): String {
        return "if($condition){$ifCommand}else{$elseCommand}"
    }
}

// ============================================================
// WhileCommand
// ============================================================

/**
 * This command mimics a normal "while" loop.
 */
class WhileCommand(
    private val condition: BooleanTerm,
    private val command: Command
) : Command() {

    override fun execute() {
        while ((condition.eval() as Boolean)) {
            command.execute()
        }
    }

    override fun toString(): String {
        return "while($condition){$command}"
    }
}

// ============================================================
// ForCommand
// ============================================================

/**
 * This command mimics a normal "for" loop.
 *
 * Example:
 * ```
 *     for (int i = 0; i < limit; i++) {
 *         // body
 *     }
 * ```
 *
 * The execute method executes:
 * ```
 *     for (setup; condition; endCommand) {
 *         bodyCommand;
 *     }
 * ```
 */
class ForCommand : Command {
    private val setupCommand: Command
    private val condition: BooleanTerm
    private val endCommand: Command
    private val bodyCommand: Command

    /**
     * Construct a "for" command that iterates the supplied
     * variable over doubles from "from" to "to", stepping by "step".
     */
    constructor(
        v: Variable,
        from: Double,
        to: Double,
        step: Double,
        bodyCommand: Command
    ) {
        val setupEv = Evaluation(v, NumberFact(from))
        this.setupCommand = AssignmentCommand(setupEv)

        this.condition = Comparison("<=", v, NumberFact(to))

        val ao = ArithmeticOperator('+', v, NumberFact(step))
        val endEv = Evaluation(v, ao)
        this.endCommand = AssignmentCommand(endEv)

        this.bodyCommand = bodyCommand
    }

    /**
     * Construct a "for" command that iterates over integers from
     * "from" to "to" (inclusive), incrementing by 1.
     */
    constructor(
        v: Variable,
        from: Int,
        to: Int,
        bodyCommand: Command
    ) : this(v, from.toDouble(), to.toDouble(), 1.0, bodyCommand)

    /**
     * Construct a "for" command from explicit setup, condition,
     * end command, and body command.
     */
    constructor(
        setupCommand: Command,
        condition: BooleanTerm,
        endCommand: Command,
        bodyCommand: Command
    ) {
        this.setupCommand = setupCommand
        this.condition = condition
        this.endCommand = endCommand
        this.bodyCommand = bodyCommand
    }

    override fun execute() {
        setupCommand.execute()
        while ((condition.eval() as Boolean)) {
            bodyCommand.execute()
            endCommand.execute()
        }
    }

    override fun toString(): String {
        return """
            for, setUpCommand: $setupCommand
                 condition:    $condition
                 endCommand:   $endCommand
                 bodyCommand:  $bodyCommand
        """.trimIndent()
    }
}
