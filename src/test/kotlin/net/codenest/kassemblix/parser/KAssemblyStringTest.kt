package net.codenest.kassemblix.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KAssemblyStringTest {

    private lateinit var assembly: KAssembly<String>

    @BeforeEach
    fun setUp() {
        assembly = KAssembly(" ")
    }

    @Test
    fun `push adds item to result stack`() {
        assembly.push("Item1")
        assertEquals("Item1", assembly.pop())
    }

    @Test
    fun `pop returns null when stack is empty`() {
        assertNull(assembly.pop())
    }

    @Test
    fun `hasMoreItem returns true when items remain`() {
        assembly.addItem("Item1").addItem("Item2")
        assembly.nextItem()
        assertTrue(assembly.hasMoreItem())
    }

    @Test
    fun `hasMoreItem returns false when no items remain`() {
        assembly.addItem("Item1")
        assembly.nextItem()
        assertFalse(assembly.hasMoreItem())
    }

    @Test
    fun `remainItemNr returns correct count`() {
        assembly.addItem("Item1").addItem("Item2").addItem("Item3")
        assembly.nextItem()
        assertEquals(2, assembly.remainItemNr())
    }

    @Test
    fun `consumedItems returns correct string`() {
        assembly.addItem("Item1").addItem("Item2").addItem("Item3")
        assembly.nextItem()
        assertEquals("Item1", assembly.consumedItems())
    }

    @Test
    fun `remainItems returns correct string`() {
        assembly.addItem("This").addItem("is").addItem("a").addItem("test")
        assembly.nextItem()
        assembly.nextItem()
        assertEquals("This is", assembly.consumedItems())
        assertEquals("a test", assembly.remainItems())
    }

    @Test
    fun `remainItems returns empty string when no items left`() {
        assembly.addItem("Only").addItem("one").addItem("item")
        while (assembly.hasMoreItem()) {
            assembly.nextItem()
        }
        assertEquals("Only one item", assembly.consumedItems())
        assertEquals("", assembly.remainItems())
    }

    @Test
    fun `remainItems returns all items when none consumed`() {
        assembly.addItem("All").addItem("items").addItem("remain")
        assertEquals("All items remain", assembly.remainItems())
    }

    @Test
    fun `remainItems returns correct string with custom delimiter`() {
        val customAssembly = KAssembly<String>("-")
        customAssembly.addItem("Custom").addItem("delimiter").addItem("test")
        customAssembly.nextItem()
        assertEquals("delimiter-test", customAssembly.remainItems())
    }

    @Test
    fun `peekItem returns first item without consuming`() {
        assembly.addItem("First").addItem("Second").addItem("Third")
        assertEquals("First", assembly.peekItem())
        assertEquals("First", assembly.peekItem()) // Ensure it doesn't consume the item
    }

    @Test
    fun `peekItem returns null when no items`() {
        assertNull(assembly.peekItem())
    }

    @Test
    fun `peekItem returns null when all items consumed`() {
        assembly.addItem("Only").addItem("one").addItem("item")
        while (assembly.hasMoreItem()) {
            assembly.nextItem()
        }
        assertNull(assembly.peekItem())
    }

    @Test
    fun `peekItem returns correct item after partial consumption`() {
        assembly.addItem("First").addItem("Second").addItem("Third")
        assembly.nextItem()
        assertEquals("Second", assembly.peekItem())
    }

    @Test
    fun `clone creates exact copy`() {
        assembly.addItem("Clone").addItem("test")
        assembly.nextItem()
        val clone = assembly.clone()
        assertEquals(assembly.toString(), clone.toString())
    }

    @Test
    fun `clone does not affect original`() {
        assembly.addItem("Original").addItem("test")
        val clone = assembly.clone()
        clone.addItem("Modified")
        assertEquals("Original test", assembly.remainItems())
        assertEquals("Original test Modified", clone.remainItems())
    }
}