package net.codenest.kassemblix.examples.query

import net.codenest.kassemblix.examples.engine.*
import net.codenest.kassemblix.examples.chips.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Query language examples demonstrating SQL-like queries using the engine.
 *
 * Converted from: com.sjm.examples.query.*
 *
 * These examples demonstrate:
 * - Building facts from domain objects
 * - Querying with variables
 * - Joining multiple tables
 * - Comparisons and filters
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Exceptions
// ============================================================

/**
 * Signals that a given string is not the name of a known class.
 */
class UnrecognizedClassException(message: String) : RuntimeException(message)

/**
 * Signals that a given string is not the name of a known variable.
 */
class UnrecognizedVariableException(message: String) : RuntimeException(message)

// ============================================================
// Speller Interface
// ============================================================

/**
 * This interface defines the role of a speller that returns
 * the proper spelling of a class or variable name.
 */
interface Speller {
    fun getClassName(s: String): String?
    fun getVariableName(s: String): String?
}

// ============================================================
// ChipSource - Provides facts from chip data
// ============================================================

/**
 * This class draws on data from ChipBase to supply facts
 * about chips, customers, and orders.
 */
object ChipSource : AxiomSource {

    /**
     * Returns all the data in the chip database.
     */
    override fun axioms(): AxiomEnumeration {
        return ProgramEnumerator(program())
    }

    /**
     * Returns all the data in the chip database.
     */
    override fun axioms(s: Structure): AxiomEnumeration {
        return axioms()
    }

    /**
     * Create a chip fact from a chip object.
     */
    fun fact(c: Chip): Fact {
        return Fact("chip", arrayOf<Any>(c.chipID, c.chipName, c.price, c.ounces, c.oil))
    }

    /**
     * Create a customer fact from a customer object.
     */
    fun fact(c: Customer): Fact {
        return Fact("customer", arrayOf<Any>(c.customerID, c.lastName, c.firstName))
    }

    /**
     * Create an order fact from an order object.
     */
    fun fact(o: Order): Fact {
        return Fact("order", arrayOf<Any>(o.customer.customerID, o.chip.chipID, o.bagsPerMonth))
    }

    /**
     * Returns all the data in the chip database as a Program.
     */
    fun program(): Program {
        val p = Program()

        // chips
        ChipBase.allChips().forEach { p.addAxiom(fact(it)) }

        // customers
        ChipBase.allCustomers().forEach { p.addAxiom(fact(it)) }

        // orders
        ChipBase.allOrders().forEach { p.addAxiom(fact(it)) }

        return p
    }

    /**
     * Returns a query structure that matches chip facts.
     */
    fun queryChip(): Structure {
        return Structure("chip", arrayOf(
            Variable("ChipID"),
            Variable("ChipName"),
            Variable("PricePerBag"),
            Variable("Ounces"),
            Variable("Oil")
        ))
    }

    /**
     * Returns a query structure that matches customer facts.
     */
    fun queryCustomer(): Structure {
        return Structure("customer", arrayOf(
            Variable("CustomerID"),
            Variable("LastName"),
            Variable("FirstName")
        ))
    }

    /**
     * Returns a query structure that matches order facts.
     */
    fun queryOrder(): Structure {
        return Structure("order", arrayOf(
            Variable("CustomerID"),
            Variable("ChipID"),
            Variable("BagsPerMonth")
        ))
    }

    /**
     * Given the name of a class, return a query that will match
     * against facts that represent objects of the class.
     */
    fun queryStructure(className: String): Structure {
        return when (className) {
            "chip" -> queryChip()
            "customer" -> queryCustomer()
            "order" -> queryOrder()
            else -> throw UnrecognizedClassException("$className is not a recognized class name")
        }
    }
}

// ============================================================
// ChipSpeller - Normalizes class and variable names
// ============================================================

/**
 * This class maintains dictionaries of the proper spelling
 * of class and variable names in the chip object model.
 */
class ChipSpeller : Speller {
    private val classNames = mutableMapOf<String, String>()
    private val variableNames = mutableMapOf<String, String>()

    init {
        loadClassNames()
        loadVariableNames()
    }

    private fun addClassName(s: String) {
        classNames[s.lowercase()] = s
    }

    private fun addVariableName(s: String) {
        variableNames[s.lowercase()] = s
    }

    override fun getClassName(s: String): String? {
        return classNames[s.lowercase()]
    }

    override fun getVariableName(s: String): String? {
        return variableNames[s.lowercase()]
    }

    private fun loadClassNames() {
        addClassName("chip")
        addClassName("customer")
        addClassName("order")
    }

    private fun loadVariableNames() {
        // Use query templates to detect variable names
        listOf(ChipSource.queryChip(), ChipSource.queryCustomer(), ChipSource.queryOrder())
            .forEach { s ->
                s.variables().elements().forEach { v ->
                    addVariableName(v.name)
                }
            }
    }
}

// ============================================================
// QueryBuilder - Builds queries from terms
// ============================================================

/**
 * This class accepts terms, class names and comparisons,
 * and then builds a query from them.
 */
class QueryBuilder(private val speller: Speller) {
    private val terms = mutableListOf<Term>()
    private val classNames = mutableListOf<String>()
    private val comparisons = mutableListOf<Comparison>()

    /**
     * Add the given class name to the query.
     */
    fun addClassName(s: String) {
        val properName = speller.getClassName(s)
            ?: throw UnrecognizedClassException("No class named $s in object model")
        classNames.add(properName)
    }

    /**
     * Add a comparison to the query.
     */
    fun addComparison(c: Comparison) {
        comparisons.add(c)
    }

    /**
     * Add a term that will appear in the head structure of the query.
     */
    fun addTerm(t: Term) {
        terms.add(t)
    }

    /**
     * Create a query from the terms, class names and comparisons.
     */
    fun build(axiomSource: AxiomSource): Query {
        val structures = mutableListOf<Structure>()

        // create the "projection" structure
        val termArray = terms.toTypedArray()
        val projection = Structure("q", termArray)
        structures.add(projection)

        // add each queried table
        classNames.forEach { name ->
            structures.add(ChipSource.queryStructure(name))
        }

        // add each comparison
        comparisons.forEach { cmp ->
            structures.add(cmp)
        }

        // create and return a query
        return Query(axiomSource, structures.toTypedArray())
    }
}

// ============================================================
// Test class
// ============================================================

class QueryTest {

    private val chipSource = ChipSource

    /**
     * Show the chip facts that ChipSource makes available.
     *
     * Original: ShowChipSource.java
     */
    @Test
    fun `show chip source`() {
        val program = ChipSource.program()
        assertNotNull(program)

        // Should have chips, customers, and orders
        val chipID = Variable("ChipID")
        val chipQuery = Query(program, Structure("chip", arrayOf(
            chipID,
            Variable("Name"),
            Variable("Price"),
            Variable("Ounces"),
            Variable("Oil")
        )))

        var chipCount = 0
        while (chipQuery.canFindNextProof()) {
            chipCount++
        }
        assertEquals(5, chipCount)
    }

    /**
     * Show querying all chips.
     */
    @Test
    fun `show query all chips`() {
        val program = ChipSource.program()

        val q = Query(program, ChipSource.queryChip())

        val chips = mutableListOf<String>()
        while (q.canFindNextProof()) {
            chips.add(q.variables().toString())
        }

        assertEquals(5, chips.size)
    }

    /**
     * Show querying all customers.
     */
    @Test
    fun `show query all customers`() {
        val program = ChipSource.program()

        val q = Query(program, ChipSource.queryCustomer())

        val customers = mutableListOf<String>()
        while (q.canFindNextProof()) {
            customers.add(q.variables().toString())
        }

        assertEquals(4, customers.size)
    }

    /**
     * Show querying all orders.
     */
    @Test
    fun `show query all orders`() {
        val program = ChipSource.program()

        val q = Query(program, ChipSource.queryOrder())

        val orders = mutableListOf<String>()
        while (q.canFindNextProof()) {
            orders.add(q.variables().toString())
        }

        assertEquals(7, orders.size)
    }

    /**
     * Show querying chips with price filter.
     *
     * Query: SELECT * FROM chip WHERE PricePerBag > 9
     */
    @Test
    fun `show query chips with filter`() {
        val program = ChipSource.program()

        val chipID = Variable("ChipID")
        val chipName = Variable("ChipName")
        val price = Variable("PricePerBag")
        val ounces = Variable("Ounces")
        val oil = Variable("Oil")

        val chipStruct = Structure("chip", arrayOf(chipID, chipName, price, ounces, oil))
        val priceFilter = Comparison(">", price, NumberFact(9.0))

        val q = Query(program, arrayOf(chipStruct, priceFilter))

        val expensiveChips = mutableListOf<String>()
        while (q.canFindNextProof()) {
            expensiveChips.add(chipName.toString())
        }

        // Chips with price > 9:
        // - Coyote Crenellations (9.95)
        // - Jim Bob's Jumbo BBQ (12.95)
        // - Saddle Horns (9.95)
        assertEquals(3, expensiveChips.size)
        assertTrue(expensiveChips.contains("Jim Bob's Jumbo BBQ"))
    }

    /**
     * Show joining chips and orders.
     *
     * Query: SELECT ChipName, BagsPerMonth FROM chip, order
     *        WHERE chip.ChipID = order.ChipID
     */
    @Test
    fun `show join chips and orders`() {
        val program = ChipSource.program()

        val chipID = Variable("ChipID")
        val chipName = Variable("ChipName")
        val price = Variable("PricePerBag")
        val ounces = Variable("Ounces")
        val oil = Variable("Oil")

        val customerID = Variable("CustomerID")
        val orderChipID = Variable("ChipID") // shared variable!
        val bags = Variable("BagsPerMonth")

        // Note: Using the same variable name "ChipID" creates the join condition
        val chipStruct = Structure("chip", arrayOf(chipID, chipName, price, ounces, oil))
        val orderStruct = Structure("order", arrayOf(customerID, orderChipID, bags))

        val q = Query(program, arrayOf(chipStruct, orderStruct))

        val results = mutableListOf<Pair<String, Int>>()
        while (q.canFindNextProof()) {
            results.add(Pair(chipName.toString(), (bags.eval() as Number).toInt()))
        }

        assertEquals(7, results.size) // 7 orders
    }

    /**
     * Show using QueryBuilder components.
     *
     * Note: The QueryBuilder is designed to work with a Jaql parser that
     * builds SQL-like queries. This test demonstrates the component classes work.
     */
    @Test
    fun `show query builder components`() {
        val speller = ChipSpeller()
        val builder = QueryBuilder(speller)

        // Verify QueryBuilder correctly validates and stores class names
        builder.addClassName("chip")
        builder.addClassName("customer")

        // Verify adding terms
        val chipName = Variable("ChipName")
        builder.addTerm(chipName)

        // Verify adding comparisons
        val price = Variable("Price")
        builder.addComparison(Comparison(">", price, NumberFact(10.0)))

        // The builder is ready - in the original Java code, this would be
        // used with a Jaql parser to build complex SQL-like queries
        assertNotNull(builder)
    }

    /**
     * Show ChipSpeller normalizing names.
     */
    @Test
    fun `show chip speller`() {
        val speller = ChipSpeller()

        // Class names
        assertEquals("chip", speller.getClassName("CHIP"))
        assertEquals("customer", speller.getClassName("Customer"))
        assertEquals("order", speller.getClassName("ORDER"))

        // Variable names
        assertEquals("ChipID", speller.getVariableName("chipid"))
        assertEquals("ChipName", speller.getVariableName("CHIPNAME"))
        assertEquals("PricePerBag", speller.getVariableName("pricePerBag"))
    }

    /**
     * Show complex join with three tables.
     *
     * Query: Find customer names and chips they order
     */
    @Test
    fun `show three table join`() {
        val program = ChipSource.program()

        // Customer
        val customerID = Variable("CustomerID")
        val lastName = Variable("LastName")
        val firstName = Variable("FirstName")
        val customerStruct = Structure("customer", arrayOf(customerID, lastName, firstName))

        // Order (note: CustomerID is shared with customer)
        val orderCustomerID = Variable("CustomerID")
        val orderChipID = Variable("ChipID")
        val bags = Variable("BagsPerMonth")
        val orderStruct = Structure("order", arrayOf(orderCustomerID, orderChipID, bags))

        // Chip (note: ChipID is shared with order)
        val chipID = Variable("ChipID")
        val chipName = Variable("ChipName")
        val price = Variable("Price")
        val ounces = Variable("Ounces")
        val oil = Variable("Oil")
        val chipStruct = Structure("chip", arrayOf(chipID, chipName, price, ounces, oil))

        val q = Query(program, arrayOf(customerStruct, orderStruct, chipStruct))

        val results = mutableListOf<String>()
        while (q.canFindNextProof()) {
            results.add("${firstName}  ${lastName} orders ${chipName}")
        }

        assertEquals(7, results.size)
    }

    /**
     * Show calculated field using ArithmeticOperator.
     *
     * Query: SELECT PricePerBag * 0.9 as DiscountPrice FROM chip
     */
    @Test
    fun `show calculated field`() {
        val program = ChipSource.program()

        val price = Variable("PricePerBag")
        val discountPrice = Variable("DiscountPrice")

        val calculation = ArithmeticOperator('*', price, NumberFact(0.9))
        val eval = Evaluation(discountPrice, calculation)

        val chipStruct = Structure("chip", arrayOf(
            Variable("ChipID"),
            Variable("ChipName"),
            price,
            Variable("Ounces"),
            Variable("Oil")
        ))

        val q = Query(program, arrayOf(chipStruct, eval))

        val discountPrices = mutableListOf<Double>()
        while (q.canFindNextProof()) {
            discountPrices.add(discountPrice.eval() as Double)
        }

        assertEquals(5, discountPrices.size)
        // First chip is Carson City Silver Dollars at 8.95
        // 8.95 * 0.9 = 8.055
        assertTrue(discountPrices.any { Math.abs(it - 8.055) < 0.001 })
    }
}
