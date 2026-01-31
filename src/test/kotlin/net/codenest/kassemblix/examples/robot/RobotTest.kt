package net.codenest.kassemblix.examples.robot

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Robot command language examples.
 *
 * Converted from: com.sjm.examples.robot.*
 *
 * Demonstrates parsing a simple robot command language and building
 * command objects from the parsed input.
 *
 * Grammar:
 * ```
 *     command      = pickCommand | placeCommand | scanCommand;
 *     pickCommand  = "pick" "carrier" "from" location;
 *     placeCommand = "place" "carrier" "at" location;
 *     scanCommand  = "scan" location;
 *     location     = Word;
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Command Classes
// ============================================================

/**
 * A robot command encapsulates work behind high-level commands.
 *
 * In a real application, execute() would send messages to
 * conveyors, track robots, etc.
 */
open class RobotCommand(var location: String = "") : Cloneable {
    open fun execute() {
        // In a real app, this would control factory equipment
    }

    public override fun clone(): RobotCommand {
        return super.clone() as RobotCommand
    }
}

/**
 * Command to pick a carrier from a location.
 */
class PickCommand(location: String = "") : RobotCommand(location) {
    override fun toString() = "pick $location"
}

/**
 * Command to place a carrier at a location.
 */
class PlaceCommand(location: String = "") : RobotCommand(location) {
    override fun toString() = "place $location"
}

/**
 * Command to scan a location.
 */
class ScanCommand(location: String = "") : RobotCommand(location) {
    override fun toString() = "scan $location"
}

// ============================================================
// Assemblers
// ============================================================

/**
 * Sets the assembly's target to a PickCommand with the location.
 */
class PickAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val cmd = PickCommand(token.sval ?: "")
        assembly.target = cmd
    }
}

/**
 * Sets the assembly's target to a PlaceCommand with the location.
 */
class PlaceAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val cmd = PlaceCommand(token.sval ?: "")
        assembly.target = cmd
    }
}

/**
 * Sets the assembly's target to a ScanCommand with the location.
 */
class ScanAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val cmd = ScanCommand(token.sval ?: "")
        assembly.target = cmd
    }
}

// ============================================================
// Parsers
// ============================================================

/**
 * A monolithic parser for robot commands.
 * Shows building a parser in a single block.
 *
 * Original: RobotMonolithic.java
 */
object RobotMonolithic {
    fun command(): KParser<KToken> {
        val command = KAlternation<KToken>()
        val pickCommand = KSequence<KToken>()
        val placeCommand = KSequence<KToken>()
        val scanCommand = KSequence<KToken>()
        val location = KWord()

        command.add(pickCommand)
        command.add(placeCommand)
        command.add(scanCommand)

        pickCommand.add(KLiteral("pick", ignoreCase = true).discard())
        pickCommand.add(KLiteral("carrier", ignoreCase = true).discard())
        pickCommand.add(KLiteral("from", ignoreCase = true).discard())
        pickCommand.add(location)

        placeCommand.add(KLiteral("place", ignoreCase = true).discard())
        placeCommand.add(KLiteral("carrier", ignoreCase = true).discard())
        placeCommand.add(KLiteral("at", ignoreCase = true).discard())
        placeCommand.add(location)

        scanCommand.add(KLiteral("scan", ignoreCase = true).discard())
        scanCommand.add(location)

        return command
    }
}

/**
 * A refactored parser that uses separate methods for each subparser.
 *
 * Original: RobotRefactored.java
 */
class RobotRefactored {
    fun command(): KParser<KToken> {
        return KAlternation<KToken>()
            .add(pickCommand())
            .add(placeCommand())
            .add(scanCommand())
    }

    private fun location(): KParser<KToken> = KWord()

    private fun pickCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("pick", ignoreCase = true).discard())
            .add(KLiteral("carrier", ignoreCase = true).discard())
            .add(KLiteral("from", ignoreCase = true).discard())
            .add(location())
    }

    private fun placeCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("place", ignoreCase = true).discard())
            .add(KLiteral("carrier", ignoreCase = true).discard())
            .add(KLiteral("at", ignoreCase = true).discard())
            .add(location())
    }

    private fun scanCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("scan", ignoreCase = true).discard())
            .add(location())
    }
}

/**
 * The full robot parser with assemblers that build command objects.
 *
 * Original: RobotParser.java
 */
class RobotParser {
    fun command(): KParser<KToken> {
        return KAlternation<KToken>()
            .add(pickCommand())
            .add(placeCommand())
            .add(scanCommand())
    }

    private fun location(): KParser<KToken> = KWord()

    private fun pickCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("pick", ignoreCase = true).discard())
            .add(KLiteral("carrier", ignoreCase = true).discard())
            .add(KLiteral("from", ignoreCase = true).discard())
            .add(location())
            .also { it.setAssembler(PickAssembler()) }
    }

    private fun placeCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("place", ignoreCase = true).discard())
            .add(KLiteral("carrier", ignoreCase = true).discard())
            .add(KLiteral("at", ignoreCase = true).discard())
            .add(location())
            .also { it.setAssembler(PlaceAssembler()) }
    }

    private fun scanCommand(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KLiteral("scan", ignoreCase = true).discard())
            .add(location())
            .also { it.setAssembler(ScanAssembler()) }
    }

    companion object {
        fun start(): KParser<KToken> = RobotParser().command()
    }
}

// ============================================================
// Test class
// ============================================================

class RobotTest {

    /**
     * Show monolithic parser.
     *
     * Original: RobotMonolithic.java
     */
    @Test
    fun `show monolithic parser`() {
        val parser = RobotMonolithic.command()
        val result = parser.bestMatch(KTokenAssembly("pick carrier from DB101_IN"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show refactored parser.
     *
     * Original: ShowRobotRefactored.java
     */
    @Test
    fun `show refactored parser`() {
        val parser = RobotRefactored().command()
        val result = parser.bestMatch(KTokenAssembly("place carrier at WB500_IN"))

        assertNotNull(result)
        assertEquals(0, result.remainItemNr())
    }

    /**
     * Show robot parser with commands.
     *
     * Original: ShowRobotParser.java
     */
    @Test
    fun `show robot parser builds commands`() {
        val parser = RobotParser.start()

        val tests = listOf(
            "pick carrier from LINE_IN" to "pick LINE_IN",
            "place carrier at DB101_IN" to "place DB101_IN",
            "pick carrier from DB101_OUT" to "pick DB101_OUT",
            "place carrier at WB500_IN" to "place WB500_IN",
            "pick carrier from WB500_OUT" to "pick WB500_OUT",
            "place carrier at LINE_OUT" to "place LINE_OUT",
            "scan DB101_OUT" to "scan DB101_OUT"
        )

        for ((input, expected) in tests) {
            val result = parser.bestMatch(KTokenAssembly(input))
            assertNotNull(result, "Should match: $input")
            val cmd = result.target as RobotCommand
            assertEquals(expected, cmd.toString())
        }
    }

    /**
     * Show pick command parsing.
     */
    @Test
    fun `show pick command`() {
        val parser = RobotParser.start()
        val result = parser.bestMatch(KTokenAssembly("pick carrier from STATION_A"))

        assertNotNull(result)
        val cmd = result.target
        assertTrue(cmd is PickCommand)
        assertEquals("STATION_A", cmd.location)
        assertEquals("pick STATION_A", cmd.toString())
    }

    /**
     * Show place command parsing.
     */
    @Test
    fun `show place command`() {
        val parser = RobotParser.start()
        val result = parser.bestMatch(KTokenAssembly("place carrier at STATION_B"))

        assertNotNull(result)
        val cmd = result.target
        assertTrue(cmd is PlaceCommand)
        assertEquals("STATION_B", cmd.location)
        assertEquals("place STATION_B", cmd.toString())
    }

    /**
     * Show scan command parsing.
     */
    @Test
    fun `show scan command`() {
        val parser = RobotParser.start()
        val result = parser.bestMatch(KTokenAssembly("scan STATION_C"))

        assertNotNull(result)
        val cmd = result.target
        assertTrue(cmd is ScanCommand)
        assertEquals("STATION_C", cmd.location)
        assertEquals("scan STATION_C", cmd.toString())
    }

    /**
     * Show case-insensitive matching.
     */
    @Test
    fun `show case insensitive matching`() {
        val parser = RobotParser.start()

        // Test various case combinations
        val tests = listOf(
            "PICK CARRIER FROM station1",
            "Pick Carrier From station2",
            "pick CARRIER from station3",
            "SCAN station4"
        )

        for (input in tests) {
            val result = parser.bestMatch(KTokenAssembly(input))
            assertNotNull(result, "Should match: $input")
            assertEquals(0, result.remainItemNr(), "Should consume all: $input")
        }
    }

    /**
     * Show command cloning.
     */
    @Test
    fun `show command cloning`() {
        val original = PickCommand("LOC_A")
        val clone = original.clone()

        assertEquals(original.location, clone.location)
        assertEquals(original.toString(), clone.toString())

        // Modify clone shouldn't affect original
        clone.location = "LOC_B"
        assertEquals("LOC_A", original.location)
        assertEquals("LOC_B", clone.location)
    }
}
