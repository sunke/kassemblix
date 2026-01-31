package net.codenest.kassemblix.examples.chips

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Domain model for a chip ordering system.
 *
 * Converted from: com.sjm.examples.chips.*
 *
 * @author Steven J. Metsker
 */

/**
 * A chip is a type of potato or corn chip that a mythical company offers.
 */
data class Chip(
    val chipID: Int,
    val chipName: String,
    val price: Double,
    val ounces: Double,
    val oil: String
) {
    override fun toString(): String =
        "chip($chipID, $chipName, $price, $ounces, $oil)"
}

/**
 * A customer has an ID, and a last and first name.
 */
data class Customer(
    val customerID: Int,
    val lastName: String,
    val firstName: String
) {
    override fun toString(): String =
        "customer($customerID, $lastName, $firstName)"
}

/**
 * An order is a standing request from a customer for a monthly supply
 * of a number of bags of a type of chip.
 */
data class Order(
    val customer: Customer,
    val chip: Chip,
    val bagsPerMonth: Int
) {
    override fun toString(): String =
        "order(${customer.customerID}, ${chip.chipID}, $bagsPerMonth)"
}

/**
 * A small database of chips, customers, and orders.
 */
object ChipBase {
    private val chips: MutableMap<Int, Chip> = mutableMapOf()
    private val customers: MutableMap<Int, Customer> = mutableMapOf()
    private val orders: MutableList<Order> = mutableListOf()

    init {
        // Initialize chips
        addChip(Chip(1001, "Carson City Silver Dollars", 8.95, 12.0, "Safflower"))
        addChip(Chip(1002, "Coyote Crenellations", 9.95, 12.0, "Coconut"))
        addChip(Chip(1003, "Four Corner Crispitos", 8.95, 12.0, "Coconut"))
        addChip(Chip(1004, "Jim Bob's Jumbo BBQ", 12.95, 16.0, "Safflower"))
        addChip(Chip(1007, "Saddle Horns", 9.95, 10.0, "Sunflower"))

        // Initialize customers
        addCustomer(Customer(11156, "Hasskins", "Hank"))
        addCustomer(Customer(11158, "Shumacher", "Carol"))
        addCustomer(Customer(12116, "Zeldis", "Kim"))
        addCustomer(Customer(12122, "Houston", "Jim"))

        // Initialize orders
        addOrder(Order(customer(11156)!!, chip(1001)!!, 2))
        addOrder(Order(customer(11156)!!, chip(1004)!!, 1))
        addOrder(Order(customer(11158)!!, chip(1007)!!, 4))
        addOrder(Order(customer(12116)!!, chip(1002)!!, 2))
        addOrder(Order(customer(12116)!!, chip(1003)!!, 2))
        addOrder(Order(customer(12122)!!, chip(1004)!!, 2))
        addOrder(Order(customer(12122)!!, chip(1007)!!, 2))
    }

    private fun addChip(c: Chip) {
        chips[c.chipID] = c
    }

    private fun addCustomer(c: Customer) {
        customers[c.customerID] = c
    }

    private fun addOrder(o: Order) {
        orders.add(o)
    }

    fun chip(id: Int): Chip? = chips[id]

    fun customer(id: Int): Customer? = customers[id]

    fun allChips(): Collection<Chip> = chips.values

    fun allCustomers(): Collection<Customer> = customers.values

    fun allOrders(): List<Order> = orders
}

/**
 * Tests demonstrating the chip domain model.
 */
class ChipsTest {

    @Test
    fun `show chip properties`() {
        val chip = ChipBase.chip(1001)
        assertNotNull(chip)
        assertEquals("Carson City Silver Dollars", chip.chipName)
        assertEquals(8.95, chip.price)
        assertEquals(12.0, chip.ounces)
        assertEquals("Safflower", chip.oil)
    }

    @Test
    fun `show customer properties`() {
        val customer = ChipBase.customer(11156)
        assertNotNull(customer)
        assertEquals("Hasskins", customer.lastName)
        assertEquals("Hank", customer.firstName)
    }

    @Test
    fun `show order structure`() {
        val orders = ChipBase.allOrders()
        assertEquals(7, orders.size)

        // Hank Hasskins ordered 2 bags of Carson City Silver Dollars
        val firstOrder = orders[0]
        assertEquals("Hasskins", firstOrder.customer.lastName)
        assertEquals("Carson City Silver Dollars", firstOrder.chip.chipName)
        assertEquals(2, firstOrder.bagsPerMonth)
    }

    @Test
    fun `show database totals`() {
        assertEquals(5, ChipBase.allChips().size)
        assertEquals(4, ChipBase.allCustomers().size)
        assertEquals(7, ChipBase.allOrders().size)
    }

    @Test
    fun `show toString formats`() {
        val chip = ChipBase.chip(1001)!!
        assertEquals("chip(1001, Carson City Silver Dollars, 8.95, 12.0, Safflower)", chip.toString())

        val customer = ChipBase.customer(11156)!!
        assertEquals("customer(11156, Hasskins, Hank)", customer.toString())

        val order = ChipBase.allOrders()[0]
        assertEquals("order(11156, 1001, 2)", order.toString())
    }
}
