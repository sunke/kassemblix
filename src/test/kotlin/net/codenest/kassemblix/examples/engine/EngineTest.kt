package net.codenest.kassemblix.examples.engine

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Logic engine examples demonstrating unification and proof.
 *
 * Converted from: com.sjm.examples.engine.*
 *
 * These examples demonstrate:
 * - Variables and structures
 * - Unification
 * - Programs and queries
 * - Rules and facts
 * - Arithmetic operations
 * - Comparisons
 * - Negation
 * - Lists
 *
 * @author Steven J. Metsker
 */
class EngineTest {

    // --------------------------------------------------------
    // Variables
    // --------------------------------------------------------

    /**
     * Show variable creation and use in structures.
     *
     * Original: ShowVariable.java
     */
    @Test
    fun `show variable`() {
        val name = Variable("Name")
        val alt = Variable("Altitude")
        val vCity = Structure("city", arrayOf(name, alt))

        assertEquals("city(Name, Altitude)", vCity.toString())
    }

    // --------------------------------------------------------
    // Structures
    // --------------------------------------------------------

    /**
     * Show a simple structure.
     *
     * Original: ShowStructure.java
     */
    @Test
    fun `show structure`() {
        val denver = Structure("denver")
        val altitude = Structure(5280)
        val city = Structure("city", arrayOf<Term>(denver, altitude))

        assertEquals("city(denver, 5280)", city.toString())
    }

    // --------------------------------------------------------
    // Facts
    // --------------------------------------------------------

    /**
     * Show facts creation.
     *
     * Original: ShowFacts.java
     */
    @Test
    fun `show facts`() {
        val fact = Fact("city", "denver", 5280)

        assertEquals("city(denver, 5280)", fact.toString())
    }

    // --------------------------------------------------------
    // Programs and Queries
    // --------------------------------------------------------

    /**
     * Return a small database of cities and their altitudes.
     */
    private fun altitudes(): Program {
        val facts = arrayOf<Axiom>(
            Fact("city", "abilene", 1718),
            Fact("city", "addis ababa", 8000),
            Fact("city", "denver", 5280),
            Fact("city", "flagstaff", 6970),
            Fact("city", "jacksonville", 8),
            Fact("city", "leadville", 10200),
            Fact("city", "madrid", 1305),
            Fact("city", "richmond", 19),
            Fact("city", "spokane", 1909),
            Fact("city", "wichita", 1305)
        )

        val p = Program()
        for (fact in facts) {
            p.addAxiom(fact)
        }
        return p
    }

    /**
     * Show the construction and use of a simple program.
     *
     * Original: ShowProgram.java
     */
    @Test
    fun `show program`() {
        val p = altitudes()

        val name = Variable("Name")
        val height = Variable("Height")
        val s = Structure("city", arrayOf(name, height))
        val q = Query(p, s)

        val cities = mutableListOf<String>()
        while (q.canFindNextProof()) {
            cities.add(name.toString())
        }

        assertEquals(10, cities.size)
        assertTrue(cities.contains("denver"))
        assertTrue(cities.contains("leadville"))
    }

    // --------------------------------------------------------
    // Proof with multiple structures
    // --------------------------------------------------------

    /**
     * Show a simple query proving itself.
     *
     * Original: ShowProof.java
     */
    @Test
    fun `show proof`() {
        val p = Program()

        // Add charge facts
        p.addAxiom(Fact("charge", "athens", 23))
        p.addAxiom(Fact("charge", "sparta", 13))
        p.addAxiom(Fact("charge", "milos", 17))

        // Add customer facts
        p.addAxiom(Fact("customer", "Marathon Marble", "sparta"))
        p.addAxiom(Fact("customer", "Acropolis Construction", "athens"))
        p.addAxiom(Fact("customer", "Agora Imports", "sparta"))

        val city = Variable("City")
        val fee = Variable("Fee")
        val name = Variable("Name")

        val s1 = Structure("charge", arrayOf(city, fee))
        val s2 = Structure("customer", arrayOf(name, city))

        // charge(City, Fee), customer(Name, City)
        val q = Query(p, arrayOf(s1, s2))

        val results = mutableListOf<Triple<String, Int, String>>()
        while (q.canFindNextProof()) {
            results.add(Triple(
                city.toString(),
                (fee.eval() as Number).toInt(),
                name.toString()
            ))
        }

        // Should find 3 matches (athens, sparta, sparta)
        assertEquals(3, results.size)
    }

    // --------------------------------------------------------
    // Rules
    // --------------------------------------------------------

    /**
     * Show a rule in action.
     *
     * Original: ShowRule.java
     */
    @Test
    fun `show rule`() {
        val p = altitudes()

        val name = Variable("Name")
        val alt = Variable("Alt")
        val fiveThou = Atom(5000)

        // highCity(Name) :- city(Name, Alt), >(Alt, 5000)
        val r = Rule(arrayOf(
            Structure("highCity", arrayOf(name)),
            Structure("city", arrayOf(name, alt)),
            Comparison(">", alt, fiveThou)
        ))

        p.addAxiom(r)

        val q = Query(p, Structure("highCity", arrayOf(name)))

        val highCities = mutableListOf<String>()
        while (q.canFindNextProof()) {
            highCities.add(name.toString())
        }

        // Cities above 5000 feet: addis ababa (8000), denver (5280),
        // flagstaff (6970), leadville (10200)
        assertEquals(4, highCities.size)
        assertTrue(highCities.contains("denver"))
        assertTrue(highCities.contains("leadville"))
        assertTrue(highCities.contains("flagstaff"))
        assertTrue(highCities.contains("addis ababa"))
    }

    // --------------------------------------------------------
    // Arithmetic
    // --------------------------------------------------------

    /**
     * Show how to perform arithmetic within the engine.
     *
     * Original: ShowArithmetic.java
     */
    @Test
    fun `show arithmetic`() {
        val a = NumberFact(1000.0)
        val b = NumberFact(999.0)

        val x = ArithmeticOperator('*', a, b)
        val y = ArithmeticOperator('+', x, b)

        assertEquals("+(*(1000.0, 999.0), 999.0)", y.toString())
        assertEquals(999999.0, y.eval())
    }

    /**
     * Show an evaluation.
     *
     * Original: ShowEvaluation.java
     */
    @Test
    fun `show evaluation`() {
        val you = Variable("You")
        val youAndBaby = Variable("YouAndBaby")
        val baby = Variable("Baby")

        val diff = ArithmeticOperator('-', youAndBaby, you)

        val e = Evaluation(baby, diff)

        you.unify(NumberFact(185.0))
        youAndBaby.unify(NumberFact(199.0))

        val result = e.canFindNextProof()
        assertTrue(result)
        assertEquals(14.0, baby.eval())
    }

    // --------------------------------------------------------
    // Comparisons
    // --------------------------------------------------------

    /**
     * Show a couple of comparisons.
     *
     * Original: ShowComparison.java
     */
    @Test
    fun `show comparison`() {
        val alt1 = Atom(5280)
        val alt2 = Atom(19)

        val q1 = Query(Comparison(">", alt1, alt2))
        assertTrue(q1.canFindNextProof())

        val q2 = Query(Comparison(">", Atom("denver"), Atom("richmond")))
        assertFalse(q2.canFindNextProof()) // "denver" < "richmond" lexically
    }

    /**
     * Show string comparison.
     */
    @Test
    fun `show string comparison`() {
        val a = Atom("alpha")
        val b = Atom("beta")

        val q = Query(Comparison("<=", a, b))
        assertTrue(q.canFindNextProof())
    }

    // --------------------------------------------------------
    // Negation
    // --------------------------------------------------------

    /**
     * Show a Not object in action.
     *
     * Original: ShowNot.java
     */
    @Test
    fun `show not`() {
        val p = Program()

        // bachelor(X) :- male(X), not married(X);
        val x = Variable("X")
        val s0 = Structure("bachelor", arrayOf<Term>(x))
        val s1 = Structure("male", arrayOf<Term>(x))
        val s2 = Not("married", arrayOf<Term>(x))
        val r0 = Rule(arrayOf(s0, s1, s2))
        p.addAxiom(r0)

        // married(jim)
        p.addAxiom(Fact("married", "jim"))

        // male(jeremy); male(jim);
        p.addAxiom(Fact("male", "jeremy"))
        p.addAxiom(Fact("male", "jim"))

        val b = Variable("B")
        val q = Query(p, Structure("bachelor", arrayOf<Term>(b)))

        val bachelors = mutableListOf<String>()
        while (q.canFindNextProof()) {
            bachelors.add(b.toString())
        }

        // Only jeremy should be a bachelor (jim is married)
        assertEquals(1, bachelors.size)
        assertEquals("jeremy", bachelors[0])
    }

    // --------------------------------------------------------
    // Lists
    // --------------------------------------------------------

    /**
     * Show some lists.
     *
     * Original: ShowList.java
     */
    @Test
    fun `show list`() {
        val snakes = Structure.list(arrayOf<Any>("cobra", "garter", "python"))
        assertEquals("[cobra, garter, python]", snakes.toString())

        // unify this list with a list of three variables
        val a = Variable("A")
        val b = Variable("B")
        val c = Variable("C")

        val abc = Structure.list(arrayOf<Term>(a, b, c))
        abc.unify(snakes)

        assertEquals("cobra", a.toString())
        assertEquals("garter", b.toString())
        assertEquals("python", c.toString())
    }

    /**
     * Show head/tail list unification.
     */
    @Test
    fun `show list head tail`() {
        val snakes = Structure.list(arrayOf<Any>("cobra", "garter", "python"))

        val head = Variable("Head")
        val tail = Variable("Tail")
        val ht = Structure.list(arrayOf<Term>(head), tail)

        ht.unify(snakes)

        assertEquals("cobra", head.toString())
        assertEquals("[garter, python]", tail.toString())
    }

    // --------------------------------------------------------
    // Unification
    // --------------------------------------------------------

    /**
     * Show variable unification.
     *
     * Original: ShowVariableUnification.java
     */
    @Test
    fun `show variable unification`() {
        val x = Variable("X")
        val denver = Atom("denver")

        val u = x.unify(denver)
        assertNotNull(u)
        assertEquals("denver", x.toString())
    }

    /**
     * Show structure unification.
     *
     * Original: ShowStructureUnification.java
     */
    @Test
    fun `show structure unification`() {
        val city = Variable("City")
        val alt = Variable("Altitude")

        val city1 = Structure("city", arrayOf(city, alt))
        val city2 = Fact("city", "denver", 5280)

        val u = city1.unify(city2)
        assertNotNull(u)
        assertEquals("denver", city.toString())
        assertEquals("5280", alt.toString())
    }

    /**
     * Show failed unification.
     *
     * Original: ShowFailedUnification.java
     */
    @Test
    fun `show failed unification`() {
        val city1 = Fact("city", "denver", 5280)
        val city2 = Fact("city", "sparta", 1000)

        val u = city1.unify(city2)
        // Should fail because denver != sparta
        kotlin.test.assertNull(u)
    }

    // --------------------------------------------------------
    // Anonymous Variables
    // --------------------------------------------------------

    /**
     * Show anonymous variable behavior.
     *
     * Original: ShowAnonymous.java
     */
    @Test
    fun `show anonymous`() {
        val anon = Anonymous()

        // Anonymous variables unify without binding
        val u1 = anon.unify(Atom("anything"))
        assertNotNull(u1)
        assertEquals(0, u1.size()) // No binding created

        val u2 = anon.unify(Structure("complex", arrayOf<Term>(Atom("term"))))
        assertNotNull(u2)
        assertEquals(0, u2.size())

        // Anonymous variable still displays as _
        assertEquals("_", anon.toString())
    }

    // --------------------------------------------------------
    // Empty List
    // --------------------------------------------------------

    /**
     * Show empty list.
     */
    @Test
    fun `show empty list`() {
        assertEquals("[]", EmptyList.toString())
        assertTrue(EmptyList.isList())
    }

    // --------------------------------------------------------
    // Two Structure Query
    // --------------------------------------------------------

    /**
     * Show a query with two structures (join).
     *
     * Original: ShowTwoStructureQuery.java
     */
    @Test
    fun `show two structure query`() {
        val p = Program()

        // Create some facts about city populations
        p.addAxiom(Fact("pop", "denver", 600000))
        p.addAxiom(Fact("pop", "boulder", 100000))

        // Create some facts about city areas
        p.addAxiom(Fact("area", "denver", 150))
        p.addAxiom(Fact("area", "boulder", 25))

        val city = Variable("City")
        val pop = Variable("Pop")
        val area = Variable("Area")

        // Query: pop(City, Pop), area(City, Area)
        val q = Query(p, arrayOf(
            Structure("pop", arrayOf(city, pop)),
            Structure("area", arrayOf(city, area))
        ))

        val results = mutableListOf<String>()
        while (q.canFindNextProof()) {
            results.add(city.toString())
        }

        assertEquals(2, results.size)
        assertTrue(results.contains("denver"))
        assertTrue(results.contains("boulder"))
    }
}
