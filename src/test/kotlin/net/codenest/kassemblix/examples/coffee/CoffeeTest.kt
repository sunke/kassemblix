package net.codenest.kassemblix.examples.coffee

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.lexer.KWordState
import net.codenest.kassemblix.parser.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Coffee shop grammar example demonstrating building domain objects from parsed text.
 *
 * Converted from: com.sjm.examples.coffee.*
 *
 * Grammar:
 * ```
 *     coffee     = name ',' roast ',' country ',' price;
 *     name       = Word (formerName | Empty);
 *     formerName = '(' Word ')';
 *     roast      = Word (orFrench | Empty);
 *     orFrench   = '/' "french";
 *     country    = Word;
 *     price      = Num;
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Domain class
// ============================================================

/**
 * A Coffee object represents a type of coffee.
 * Each type of coffee has a name, and may have a former name.
 * Each type also has a roast, which may be Regular, French, or Italian.
 * We offer some types of coffee in two roasts, where French is an
 * alternative to the normal roast. Finally, each type has a country
 * of origin, and a price per pound.
 */
data class Coffee(
    var name: String = "",
    var formerName: String? = null,
    var roast: String = "",
    var alsoOfferFrench: Boolean = false,
    var country: String = "",
    var price: Double = 0.0
) {
    override fun toString(): String {
        val buf = StringBuilder()
        buf.append(name)
        formerName?.let {
            buf.append("($it)")
        }
        buf.append(", ")
        buf.append(roast)
        if (alsoOfferFrench) {
            buf.append("/French")
        }
        buf.append(", ")
        buf.append(country)
        buf.append(", ")
        buf.append(price)
        return buf.toString()
    }
}

// ============================================================
// Assemblers
// ============================================================

/**
 * Pops a coffee's name from the stack and sets the assembly's target
 * to be a new Coffee object with this name.
 */
class NameAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val coffee = Coffee()
        val token = assembly.pop() as KToken
        coffee.name = token.sval?.trim() ?: ""
        assembly.target = coffee
    }
}

/**
 * Pops a string and sets the target coffee's former name.
 */
class FormerNameAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val coffee = assembly.target as Coffee
        coffee.formerName = token.sval?.trim()
    }
}

/**
 * Pops a string and sets the target coffee's roast.
 */
class RoastAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val coffee = assembly.target as Coffee
        coffee.roast = token.sval?.trim() ?: ""
    }
}

/**
 * Sets the target coffee's alsoOfferFrench flag to true.
 */
class AlsoFrenchAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val coffee = assembly.target as Coffee
        coffee.alsoOfferFrench = true
    }
}

/**
 * Pops a string and sets the target coffee's country.
 */
class CountryAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val coffee = assembly.target as Coffee
        coffee.country = token.sval?.trim() ?: ""
    }
}

/**
 * Pops a number and sets the target coffee's price.
 */
class PriceAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val token = assembly.pop() as KToken
        val coffee = assembly.target as Coffee
        coffee.price = token.nval
    }
}

// ============================================================
// Parser
// ============================================================

/**
 * A parser that recognizes a textual description of a type of coffee,
 * and builds a corresponding Coffee object.
 */
class CoffeeParser {

    /**
     * Return a parser that recognizes:
     *     coffee = name ',' roast ',' country ',' price;
     */
    fun coffee(): KParser<KToken> {
        val comma = KSymbol(',').discard()
        return KSequence<KToken>()
            .add(name())
            .add(comma)
            .add(roast())
            .add(comma)
            .add(country())
            .add(comma)
            .add(price())
    }

    /**
     * Return a parser that recognizes:
     *     name = Word (formerName | Empty);
     */
    private fun name(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KWord().setAssembler(NameAssembler()))
            .add(KAlternation<KToken>()
                .add(formerName())
                .add(KEmpty()))
    }

    /**
     * Return a parser that recognizes:
     *     formerName = '(' Word ')';
     */
    private fun formerName(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KSymbol('(').discard())
            .add(KWord().setAssembler(FormerNameAssembler()))
            .add(KSymbol(')').discard())
    }

    /**
     * Return a parser that recognizes:
     *     roast = Word (orFrench | Empty);
     */
    private fun roast(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KWord().setAssembler(RoastAssembler()))
            .add(KAlternation<KToken>()
                .add(orFrench())
                .add(KEmpty()))
    }

    /**
     * Return a parser that recognizes:
     *     orFrench = '/' "french";
     */
    private fun orFrench(): KParser<KToken> {
        return KSequence<KToken>()
            .add(KSymbol('/').discard())
            .add(KLiteral("french", ignoreCase = true).discard())
            .also { it.setAssembler(AlsoFrenchAssembler()) }
    }

    /**
     * Return a parser that recognizes:
     *     country = Word;
     */
    private fun country(): KParser<KToken> {
        return KWord().setAssembler(CountryAssembler())
    }

    /**
     * Return a parser that recognizes:
     *     price = Num;
     */
    private fun price(): KParser<KToken> {
        return KNum().setAssembler(PriceAssembler())
    }

    companion object {
        fun start(): KParser<KToken> = CoffeeParser().coffee()
    }
}

// ============================================================
// Test class
// ============================================================

class CoffeeTest {

    /**
     * Show basic coffee parsing.
     *
     * Original: ShowCoffee.java
     */
    @Test
    fun `show coffee parsing`() {
        // Enable spaces in word tokens for coffee names like "Briton Blast"
        KWordState.setBlankAllowed(true)

        try {
            val parser = CoffeeParser.start()

            // Simple coffee
            val result1 = parser.bestMatch(KTokenAssembly("Briton Blast, Italian, Kenya, 6.95"))
            assertNotNull(result1)
            val coffee1 = result1.target as Coffee
            assertEquals("Briton Blast", coffee1.name)
            assertEquals("Italian", coffee1.roast)
            assertEquals("Kenya", coffee1.country)
            assertEquals(6.95, coffee1.price)
        } finally {
            KWordState.setBlankAllowed(false)
        }
    }

    /**
     * Show coffee with former name.
     */
    @Test
    fun `show coffee with former name`() {
        KWordState.setBlankAllowed(true)

        try {
            val parser = CoffeeParser.start()

            val result = parser.bestMatch(KTokenAssembly("Antigua Sunrise (Guatalan Delight), Regular, Guatemala, 7.25"))
            assertNotNull(result)
            val coffee = result.target as Coffee
            assertEquals("Antigua Sunrise", coffee.name)
            assertEquals("Guatalan Delight", coffee.formerName)
            assertEquals("Regular", coffee.roast)
            assertEquals("Guatemala", coffee.country)
            assertEquals(7.25, coffee.price)
        } finally {
            KWordState.setBlankAllowed(false)
        }
    }

    /**
     * Show coffee with French roast alternative.
     */
    @Test
    fun `show coffee with french roast alternative`() {
        KWordState.setBlankAllowed(true)

        try {
            val parser = CoffeeParser.start()

            val result = parser.bestMatch(KTokenAssembly("Sumatra Sunset, Regular/French, Sumatra, 7.50"))
            assertNotNull(result)
            val coffee = result.target as Coffee
            assertEquals("Sumatra Sunset", coffee.name)
            assertEquals("Regular", coffee.roast)
            assertTrue(coffee.alsoOfferFrench)
            assertEquals("Sumatra", coffee.country)
            assertEquals(7.50, coffee.price)
        } finally {
            KWordState.setBlankAllowed(false)
        }
    }

    /**
     * Show coffee toString format.
     */
    @Test
    fun `show coffee toString format`() {
        val coffee = Coffee(
            name = "Test Coffee",
            formerName = "Old Name",
            roast = "Dark",
            alsoOfferFrench = true,
            country = "Brazil",
            price = 8.99
        )

        assertEquals("Test Coffee(Old Name), Dark/French, Brazil, 8.99", coffee.toString())
    }

    /**
     * Show coffee without former name or french alternative.
     */
    @Test
    fun `show simple coffee toString`() {
        val coffee = Coffee(
            name = "Simple Bean",
            roast = "Light",
            country = "Colombia",
            price = 5.50
        )

        assertEquals("Simple Bean, Light, Colombia, 5.5", coffee.toString())
    }

    /**
     * Show multiple coffee types can be parsed.
     *
     * Original: ShowCoffee.java (reading from file)
     */
    @Test
    fun `show parsing multiple coffee types`() {
        KWordState.setBlankAllowed(true)

        try {
            val parser = CoffeeParser.start()

            val coffeeDescriptions = listOf(
                "Brimful, Regular, Colombia, 6.95",
                "Caramel Nut, Regular/French, Sumatra, 7.95",
                "Hawaii Kona, Light, Hawaii, 11.95"
            )

            val coffees = coffeeDescriptions.map { desc ->
                val result = parser.bestMatch(KTokenAssembly(desc))
                assertNotNull(result)
                result.target as Coffee
            }

            assertEquals(3, coffees.size)
            assertEquals("Brimful", coffees[0].name)
            assertEquals("Caramel Nut", coffees[1].name)
            assertTrue(coffees[1].alsoOfferFrench)
            assertEquals("Hawaii Kona", coffees[2].name)
            assertEquals(11.95, coffees[2].price, 0.001)
        } finally {
            KWordState.setBlankAllowed(false)
        }
    }
}
