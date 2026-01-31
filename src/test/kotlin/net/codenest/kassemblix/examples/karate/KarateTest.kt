package net.codenest.kassemblix.examples.karate

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Karate puzzle solving example using permutations.
 *
 * Converted from: com.sjm.examples.karate.*
 *
 * The puzzle: Each of four martial arts students has a different
 * specialty. From the following clues, determine each student's
 * full name and special skill.
 *
 * Clues:
 * 1. Ms. Ellis (whose instructor is Mr. Caldwell), Amy, and Ms. Fowler
 *    are all martial arts students.
 * 2. Sparring isn't the specialty of either Carla or Dianne.
 * 3. Neither the shoot-fighting expert nor the pressure point fighter
 *    is named Fowler.
 * 4. Children's techniques aren't the specialty of Dianne
 *    (whose instructor is Ms. Sherman).
 * 5. Amy, who disdains pressure point fighting, isn't Ms. Goodrich.
 * 6. Betti and Ms. Fowler are roommates.
 * 7. Ms. Hightower avoids sparring because of its point scoring nature.
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Helper classes
// ============================================================

/**
 * A karate student with first name, last name, and specialty.
 */
data class Student(
    val firstName: String,
    var lastName: String = "",
    var specialty: String = ""
) {
    override fun toString(): String = "$firstName $lastName: $specialty"
}

/**
 * Generates all permutations of a list.
 */
fun <T> permutations(list: List<T>): Sequence<List<T>> = sequence {
    if (list.size <= 1) {
        yield(list)
    } else {
        for (i in list.indices) {
            val element = list[i]
            val remaining = list.subList(0, i) + list.subList(i + 1, list.size)
            for (perm in permutations(remaining)) {
                yield(listOf(element) + perm)
            }
        }
    }
}

/**
 * The karate puzzle solver.
 */
class KaratePuzzle {
    private val amy = Student("Amy")
    private val betti = Student("Betti")
    private val carla = Student("Carla")
    private val dianne = Student("Dianne")
    private val students = listOf(amy, betti, carla, dianne)

    private val lastNames = listOf("Ellis", "Fowler", "Goodrich", "Hightower")
    private val specialties = listOf("Sparring", "Shoot Fighting", "Pressure Points", "Childrens")

    /**
     * Set the student objects' last names and specialties from the provided lists.
     */
    private fun assembleStudents(lasts: List<String>, specs: List<String>) {
        for (i in students.indices) {
            students[i].lastName = lasts[i]
            students[i].specialty = specs[i]
        }
    }

    /**
     * Find student by last name.
     */
    private fun studentNamed(lastName: String): Student {
        return students.find { it.lastName == lastName }
            ?: throw IllegalStateException("Bad last name: $lastName")
    }

    /**
     * Check if current student configuration satisfies all clues.
     */
    private fun cluesVerify(): Boolean {
        return (
            // Clue 1: Ms. Ellis, Amy, and Ms. Fowler are different people
            amy.lastName != "Ellis" && amy.lastName != "Fowler" &&
            // Clue 2: Sparring isn't Carla's or Dianne's specialty
            carla.specialty != "Sparring" &&
            dianne.specialty != "Sparring" &&
            // Clue 3: Fowler isn't shoot-fighting or pressure point expert
            studentNamed("Fowler").specialty != "Shoot Fighting" &&
            studentNamed("Fowler").specialty != "Pressure Points" &&
            // Clue 4: Children's techniques aren't Dianne's specialty
            dianne.specialty != "Childrens" &&
            // Clue 5: Amy isn't Ms. Goodrich and doesn't do pressure points
            amy.lastName != "Goodrich" &&
            amy.specialty != "Pressure Points" &&
            // Clue 6: Betti and Ms. Fowler are roommates (different people)
            betti.lastName != "Fowler" &&
            // Clue 7: Ms. Hightower doesn't do sparring
            studentNamed("Hightower").specialty != "Sparring" &&
            // Clue 4 + 1: Dianne's instructor is Ms. Sherman, Ellis's is Mr. Caldwell
            dianne.lastName != "Ellis"
        )
    }

    /**
     * Solve the puzzle by trying all permutations.
     */
    fun solve(): List<List<Student>> {
        val solutions = mutableListOf<List<Student>>()

        for (lastNamePerm in permutations(lastNames)) {
            for (specialtyPerm in permutations(specialties)) {
                assembleStudents(lastNamePerm, specialtyPerm)
                if (cluesVerify()) {
                    // Save a copy of the solution
                    solutions.add(students.map { it.copy() })
                }
            }
        }

        return solutions
    }
}

// ============================================================
// Test class
// ============================================================

class KarateTest {

    /**
     * Show the puzzle has exactly one solution.
     *
     * Original: KaratePuzzle.java
     */
    @Test
    fun `solve karate puzzle`() {
        val puzzle = KaratePuzzle()
        val solutions = puzzle.solve()

        // The puzzle should have exactly one solution
        assertEquals(1, solutions.size)

        val solution = solutions[0]

        // Verify solution by checking known constraints
        val amy = solution.find { it.firstName == "Amy" }!!
        val betti = solution.find { it.firstName == "Betti" }!!
        val carla = solution.find { it.firstName == "Carla" }!!
        val dianne = solution.find { it.firstName == "Dianne" }!!

        // Verify clue 1: Amy is neither Ellis nor Fowler
        assertTrue(amy.lastName != "Ellis" && amy.lastName != "Fowler")

        // Verify clue 2: Neither Carla nor Dianne does Sparring
        assertTrue(carla.specialty != "Sparring")
        assertTrue(dianne.specialty != "Sparring")

        // Verify clue 6: Betti is not Fowler
        assertTrue(betti.lastName != "Fowler")
    }

    /**
     * Show the expected solution details.
     */
    @Test
    fun `verify solution details`() {
        val puzzle = KaratePuzzle()
        val solutions = puzzle.solve()
        val solution = solutions[0]

        // Print solution for verification
        solution.forEach { println(it) }

        val amy = solution.find { it.firstName == "Amy" }!!
        val betti = solution.find { it.firstName == "Betti" }!!
        val carla = solution.find { it.firstName == "Carla" }!!
        val dianne = solution.find { it.firstName == "Dianne" }!!

        // The actual solution:
        // Amy Hightower: Shoot Fighting
        // Betti Ellis: Sparring
        // Carla Fowler: Childrens
        // Dianne Goodrich: Pressure Points

        assertEquals("Hightower", amy.lastName)
        assertEquals("Shoot Fighting", amy.specialty)

        assertEquals("Ellis", betti.lastName)
        assertEquals("Sparring", betti.specialty)

        assertEquals("Fowler", carla.lastName)
        assertEquals("Childrens", carla.specialty)

        assertEquals("Goodrich", dianne.lastName)
        assertEquals("Pressure Points", dianne.specialty)
    }

    /**
     * Show permutation generator works correctly.
     */
    @Test
    fun `show permutation generator`() {
        val perms = permutations(listOf(1, 2, 3)).toList()

        assertEquals(6, perms.size)  // 3! = 6
        assertTrue(perms.contains(listOf(1, 2, 3)))
        assertTrue(perms.contains(listOf(1, 3, 2)))
        assertTrue(perms.contains(listOf(2, 1, 3)))
        assertTrue(perms.contains(listOf(2, 3, 1)))
        assertTrue(perms.contains(listOf(3, 1, 2)))
        assertTrue(perms.contains(listOf(3, 2, 1)))
    }
}
