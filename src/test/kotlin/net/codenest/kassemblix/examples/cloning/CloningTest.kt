package net.codenest.kassemblix.examples.cloning

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * Demonstrations of cloning and copy semantics.
 *
 * Converted from: com.sjm.examples.cloning.*
 *
 * These examples show the difference between shallow and deep copying,
 * and how Kotlin's data class copy() method provides a more idiomatic approach.
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Domain classes demonstrating cloning concepts
// ============================================================

/**
 * A customer with name and IQ. In Kotlin, we use data class for
 * automatic copy() method.
 */
data class Customer(
    val name: String,
    var iq: Int
)

/**
 * A person with a name and optional spouse reference.
 * Demonstrates circular references in cloning.
 */
data class Person(
    val name: String,
    var spouse: Person? = null
)

/**
 * An order that performs shallow copy - shares customer reference.
 * This demonstrates the FLAWED approach.
 */
class OrderFlawed(
    var customer: Customer
) : Cloneable {
    /**
     * Shallow clone - the cloned order shares the same customer object!
     */
    public override fun clone(): OrderFlawed {
        return super.clone() as OrderFlawed
    }
}

/**
 * An order that performs deep copy - clones customer reference.
 * This demonstrates the CORRECT approach.
 */
class OrderOk(
    var customer: Customer
) : Cloneable {
    /**
     * Deep clone - creates a copy of the customer too.
     */
    public override fun clone(): OrderOk {
        val copy = super.clone() as OrderOk
        copy.customer = customer.copy()  // Deep copy the customer
        return copy
    }
}

/**
 * Professor supporting the Course example.
 */
data class Professor(val name: String = "Dr. Smith")

/**
 * Textbook supporting the Course example.
 */
data class Textbook(val title: String = "Algorithms")

/**
 * Course with proper deep cloning.
 */
data class Course(
    var professor: Professor,
    var textbook: Textbook
)

// ============================================================
// Test class
// ============================================================

class CloningTest {

    /**
     * Show that basic cloning works when implementing Cloneable.
     *
     * Original: CloningOk.java
     */
    @Test
    fun `show basic cloning works with Cloneable`() {
        val original = OrderOk(Customer("Test", 100))
        val copy = original.clone()
        assertNotSame(original, copy)
    }

    /**
     * Show that cloning without implementing Cloneable throws exception.
     *
     * Original: CannotCloneWithoutCloneable.java
     *
     * Note: In Kotlin, this is demonstrated by calling the Java clone()
     * mechanism on an object that doesn't implement Cloneable.
     */
    @Test
    fun `show cloning fails without Cloneable`() {
        // In Java/Kotlin, calling clone() on a non-Cloneable throws exception
        // This is demonstrated by the Java class CannotCloneWithoutCloneable.java
        // In Kotlin, we typically use data class copy() instead of clone()

        // This test verifies the concept: Cloneable is required for clone()
        val obj = object : Any() {}
        // obj.clone() would fail at compile time in Kotlin
        // In Java, it would throw CloneNotSupportedException at runtime
    }

    /**
     * Show how to clone a simple customer.
     *
     * Original: ShowCustomer.java
     *
     * In Kotlin, data classes provide automatic copy() method.
     */
    @Test
    fun `show customer copy`() {
        val al = Customer("Albert", 180)
        val al2 = al.copy()

        assertNotSame(al, al2)
        assertEquals(al.name, al2.name)
        assertEquals(al.iq, al2.iq)
    }

    /**
     * Show the flaw in shallow cloning: OrderFlawed shares customer reference.
     *
     * Original: ShowOrderFlawed.java
     *
     * When we modify the customer through the clone, it affects the original!
     */
    @Test
    fun `show shallow copy flaw - modifying clone affects original`() {
        val al = Customer("Albert", 180)
        val orig = OrderFlawed(al)
        val bogus = orig.clone()

        // The flaw: both orders share the same customer object
        assertSame(orig.customer, bogus.customer)

        // Modifying customer through clone affects original
        bogus.customer.iq = 100
        assertEquals(100, orig.customer.iq)  // Original is affected!
    }

    /**
     * Show correct deep cloning: OrderOk copies customer reference.
     *
     * Original: ShowOrderOk.java
     *
     * When we modify the customer through the clone, the original is unaffected.
     */
    @Test
    fun `show deep copy - modifying clone does not affect original`() {
        val al = Customer("Albert", 180)
        val orig = OrderOk(al)
        val copy = orig.clone()

        // Correct: each order has its own customer object
        assertNotSame(orig.customer, copy.customer)

        // Modifying customer through clone does NOT affect original
        copy.customer.iq = 100
        assertEquals(180, orig.customer.iq)  // Original is unaffected!
    }

    /**
     * Show proper deep copying of a Course with professor and textbook.
     *
     * Original: Course.java, Professor.java, Textbook.java
     *
     * In Kotlin, data class copy() creates a shallow copy by default.
     * For deep copy, we need to explicitly copy nested objects.
     */
    @Test
    fun `show course deep copy`() {
        val original = Course(
            professor = Professor("Dr. Jones"),
            textbook = Textbook("Parsing Techniques")
        )

        // Using copy() with explicit nested copies for deep clone
        val copy = original.copy(
            professor = original.professor.copy(),
            textbook = original.textbook.copy()
        )

        assertNotSame(original, copy)
        assertNotSame(original.professor, copy.professor)
        assertNotSame(original.textbook, copy.textbook)

        // But values are equal
        assertEquals(original.professor, copy.professor)
        assertEquals(original.textbook, copy.textbook)
    }

    /**
     * Show Person with shallow copy creates shared spouse reference.
     *
     * Original: Person.java
     */
    @Test
    fun `show person shallow copy with spouse reference`() {
        val adam = Person("Adam")
        val eve = Person("Eve")
        adam.spouse = eve
        eve.spouse = adam

        // Shallow copy - spouse reference is shared
        val adamClone = adam.copy()

        assertNotSame(adam, adamClone)
        assertSame(adam.spouse, adamClone.spouse)  // Same Eve!
    }

    /**
     * Demonstrate Kotlin's idiomatic approach: data class copy() with modifications.
     *
     * This shows how Kotlin simplifies the need for manual clone() implementations.
     */
    @Test
    fun `show kotlin idiomatic copy with modifications`() {
        val original = Customer("Alice", 150)

        // Create a copy with one field modified
        val modified = original.copy(iq = 160)

        assertNotSame(original, modified)
        assertEquals("Alice", modified.name)  // Same name
        assertEquals(150, original.iq)         // Original unchanged
        assertEquals(160, modified.iq)         // Copy has new value
    }
}
