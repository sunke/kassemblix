package net.codenest.kassemblix.examples.sling

import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Sling language examples - Non-UI core functionality.
 *
 * Converted from: com.sjm.examples.sling.*
 *
 * Sling is a DSL for plotting parametric curves. This test file
 * demonstrates the core function composition concepts without
 * the UI components (SlingIde, SlingPanel, Slider, etc.).
 *
 * The Sling language allows composing mathematical functions to
 * describe curves. Time (t) varies from 0 to 1 as a plot unfolds.
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Core Classes
// ============================================================

/**
 * A 2D point with x and y coordinates.
 *
 * Original: Point.java
 */
data class SlingPoint(var x: Double, var y: Double) {
    override fun toString() = "($x, $y)"
}

/**
 * Stores extreme values (min and max) for a function.
 *
 * Original: Extrema.java
 */
data class Extrema(val min: SlingPoint, val max: SlingPoint)

/**
 * Exception for using unassigned variables.
 *
 * Original: UnassignedVariableException.java
 */
class UnassignedVariableException(message: String) : RuntimeException(message)

/**
 * Abstract base class for Sling functions.
 *
 * Original: SlingFunction.java
 *
 * A SlingFunction computes a 2D point for a given time t (0 to 1).
 * Functions can be composed by wrapping other functions.
 */
abstract class SlingFunction(protected val source: Array<SlingFunction> = emptyArray()) : Cloneable {

    constructor(source: SlingFunction) : this(arrayOf(source))
    constructor(source0: SlingFunction, source1: SlingFunction) : this(arrayOf(source0, source1))

    /**
     * The core function - returns a point for the given time.
     * Time goes from 0 to 1 as a plot unfolds.
     */
    abstract fun f(t: Double): SlingPoint

    /**
     * Evaluates variables in the function to concrete values.
     */
    open fun eval(): SlingFunction {
        val f = fresh()
        for (i in source.indices) {
            f.source[i] = source[i].eval()
        }
        return f
    }

    /**
     * Creates a copy with an empty source array of the right length.
     */
    open fun fresh(): SlingFunction {
        val clone = clone() as SlingFunction
        return clone
    }

    /**
     * Returns the extreme values this function reaches.
     */
    fun extrema(nPoint: Int): Extrema {
        var min: SlingPoint? = null
        var max: SlingPoint? = null

        for (i in 0 until nPoint) {
            val t = i.toDouble() / (nPoint - 1)
            val p = f(t)
            if (i == 0) {
                min = SlingPoint(p.x, p.y)
                max = SlingPoint(p.x, p.y)
            } else {
                min!!.x = minOf(min.x, p.x)
                min.y = minOf(min.y, p.y)
                max!!.x = maxOf(max.x, p.x)
                max.y = maxOf(max.y, p.y)
            }
        }
        return Extrema(min!!, max!!)
    }

    public override fun clone(): Any = super.clone()
}

// ============================================================
// Basic Functions
// ============================================================

/**
 * The time function - returns (t, t) for time t.
 *
 * Original: T.java
 */
class T : SlingFunction() {
    override fun f(t: Double) = SlingPoint(t, t)
    override fun toString() = "t"
}

/**
 * A constant point function.
 *
 * Original: Point.java (extended from SlingFunction)
 */
class ConstPoint(private val x: Double, private val y: Double) : SlingFunction() {
    override fun f(t: Double) = SlingPoint(x, y)
    override fun toString() = "($x, $y)"
}

// ============================================================
// Arithmetic Functions
// ============================================================

/**
 * Arithmetic operations on two functions.
 *
 * Original: Arithmetic.java
 */
class Arithmetic(
    private val operator: Char,
    source0: SlingFunction = T(),
    source1: SlingFunction = T()
) : SlingFunction(source0, source1) {

    private fun arithmetic(a: Double, b: Double): Double = when (operator) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> a / b
        '%' -> a % b
        else -> 0.0
    }

    override fun f(t: Double): SlingPoint {
        val a = source[0].f(t)
        val b = source[1].f(t)
        return SlingPoint(arithmetic(a.x, b.x), arithmetic(a.y, b.y))
    }

    override fun fresh(): SlingFunction = Arithmetic(operator)

    override fun toString() = "$operator(${source[0]}, ${source[1]})"
}

// ============================================================
// Trigonometric Functions
// ============================================================

/**
 * Sin function wrapper.
 *
 * Original: Sin.java
 */
class Sin(source: SlingFunction = T()) : SlingFunction(source) {
    override fun f(t: Double): SlingPoint {
        val p = source[0].f(t)
        return SlingPoint(t, sin(p.y))
    }

    override fun fresh(): SlingFunction = Sin()
    override fun toString() = "sin(${source[0]})"
}

/**
 * Cos function wrapper.
 *
 * Original: Cos.java
 */
class Cos(source: SlingFunction = T()) : SlingFunction(source) {
    override fun f(t: Double): SlingPoint {
        val p = source[0].f(t)
        return SlingPoint(t, cos(p.y))
    }

    override fun fresh(): SlingFunction = Cos()
    override fun toString() = "cos(${source[0]})"
}

// ============================================================
// Other Math Functions
// ============================================================

/**
 * Absolute value function.
 *
 * Original: Abs.java
 */
class Abs(source: SlingFunction = T()) : SlingFunction(source) {
    override fun f(t: Double): SlingPoint {
        val p = source[0].f(t)
        return SlingPoint(t, abs(p.y))
    }

    override fun fresh(): SlingFunction = Abs()
    override fun toString() = "abs(${source[0]})"
}

/**
 * Floor function.
 *
 * Original: Floor.java
 */
class Floor(source: SlingFunction = T()) : SlingFunction(source) {
    override fun f(t: Double): SlingPoint {
        val p = source[0].f(t)
        return SlingPoint(t, kotlin.math.floor(p.y))
    }

    override fun fresh(): SlingFunction = Floor()
    override fun toString() = "floor(${source[0]})"
}

/**
 * Ceiling function.
 *
 * Original: Ceil.java
 */
class Ceil(source: SlingFunction = T()) : SlingFunction(source) {
    override fun f(t: Double): SlingPoint {
        val p = source[0].f(t)
        return SlingPoint(t, kotlin.math.ceil(p.y))
    }

    override fun fresh(): SlingFunction = Ceil()
    override fun toString() = "ceil(${source[0]})"
}

// ============================================================
// Complex Functions
// ============================================================

/**
 * Cartesian coordinates from two functions.
 *
 * Original: Cartesian.java
 */
class Cartesian(
    source0: SlingFunction = T(),
    source1: SlingFunction = T()
) : SlingFunction(source0, source1) {

    override fun f(t: Double): SlingPoint {
        val p0 = source[0].f(t)
        val p1 = source[1].f(t)
        return SlingPoint(p0.y, p1.y)
    }

    override fun fresh(): SlingFunction = Cartesian()
    override fun toString() = "cartesian(${source[0]}, ${source[1]})"
}

/**
 * Polar coordinates from radius and angle functions.
 *
 * Original: Polar.java
 */
class Polar(
    source0: SlingFunction = T(),
    source1: SlingFunction = T()
) : SlingFunction(source0, source1) {

    override fun f(t: Double): SlingPoint {
        val r = source[0].f(t).y
        val theta = source[1].f(t).y
        return SlingPoint(r * cos(theta), r * sin(theta))
    }

    override fun fresh(): SlingFunction = Polar()
    override fun toString() = "polar(${source[0]}, ${source[1]})"
}

/**
 * The Sling function - simulates a stone on a sling.
 *
 * Original: Sling.java
 *
 * sling(r, n) = polar(r, 2*pi*n*t)
 */
class Sling(
    source0: SlingFunction = T(),
    source1: SlingFunction = T()
) : SlingFunction(source0, source1) {

    override fun f(t: Double): SlingPoint {
        val r = source[0].f(t).y
        val n = source[1].f(t).y
        val theta = 2.0 * PI * n * t
        return SlingPoint(r * cos(theta), r * sin(theta))
    }

    override fun fresh(): SlingFunction = Sling()
    override fun toString() = "sling(${source[0]}, ${source[1]})"
}

// ============================================================
// Variable
// ============================================================

/**
 * A named variable that can store a function.
 *
 * Original: Variable.java
 */
class Variable(private val name: String) : SlingFunction(arrayOf<SlingFunction>()) {
    private var value: SlingFunction? = null

    fun setValue(f: SlingFunction) {
        value = f
    }

    override fun eval(): SlingFunction {
        return value?.eval()
            ?: throw UnassignedVariableException("> Program uses $name before assigning it a value.")
    }

    override fun f(t: Double): SlingPoint {
        throw InternalError("Variables cannot be plotted directly")
    }

    override fun fresh(): SlingFunction = Variable(name)

    override fun toString(): String {
        return if (value == null) name else "$name = $value"
    }
}

// ============================================================
// Test class
// ============================================================

class SlingTest {

    private val tolerance = 0.0001

    /**
     * Show basic time function.
     */
    @Test
    fun `show time function`() {
        val t = T()

        assertEquals(SlingPoint(0.0, 0.0), t.f(0.0))
        assertEquals(SlingPoint(0.5, 0.5), t.f(0.5))
        assertEquals(SlingPoint(1.0, 1.0), t.f(1.0))
    }

    /**
     * Show constant point function.
     */
    @Test
    fun `show constant point`() {
        val p = ConstPoint(3.0, 4.0)

        assertEquals(SlingPoint(3.0, 4.0), p.f(0.0))
        assertEquals(SlingPoint(3.0, 4.0), p.f(0.5))
        assertEquals(SlingPoint(3.0, 4.0), p.f(1.0))
    }

    /**
     * Show arithmetic addition.
     */
    @Test
    fun `show arithmetic addition`() {
        val sum = Arithmetic('+', ConstPoint(1.0, 2.0), ConstPoint(3.0, 4.0))
        val result = sum.f(0.5)

        assertEquals(4.0, result.x, tolerance)
        assertEquals(6.0, result.y, tolerance)
    }

    /**
     * Show arithmetic with time.
     */
    @Test
    fun `show arithmetic with time`() {
        // t + t = 2t
        val sum = Arithmetic('+', T(), T())

        val r0 = sum.f(0.0)
        assertEquals(0.0, r0.x, tolerance)
        assertEquals(0.0, r0.y, tolerance)

        val r1 = sum.f(0.5)
        assertEquals(1.0, r1.x, tolerance)
        assertEquals(1.0, r1.y, tolerance)
    }

    /**
     * Show sin function.
     */
    @Test
    fun `show sin function`() {
        val sinT = Sin(T())

        val r0 = sinT.f(0.0)
        assertEquals(0.0, r0.x, tolerance)
        assertEquals(0.0, r0.y, tolerance)

        val rPi2 = sinT.f(PI / 2)
        assertEquals(PI / 2, rPi2.x, tolerance)
        assertEquals(1.0, rPi2.y, tolerance)
    }

    /**
     * Show cos function.
     */
    @Test
    fun `show cos function`() {
        val cosT = Cos(T())

        val r0 = cosT.f(0.0)
        assertEquals(0.0, r0.x, tolerance)
        assertEquals(1.0, r0.y, tolerance)

        val rPi = cosT.f(PI)
        assertEquals(PI, rPi.x, tolerance)
        assertEquals(-1.0, rPi.y, tolerance)
    }

    /**
     * Show polar coordinates.
     */
    @Test
    fun `show polar coordinates`() {
        // polar(1, 0) = (1, 0)
        val polar = Polar(ConstPoint(0.0, 1.0), ConstPoint(0.0, 0.0))
        val result = polar.f(0.0)

        assertEquals(1.0, result.x, tolerance)
        assertEquals(0.0, result.y, tolerance)

        // polar(1, pi/2) = (0, 1)
        val polar2 = Polar(ConstPoint(0.0, 1.0), ConstPoint(0.0, PI / 2))
        val result2 = polar2.f(0.0)

        assertEquals(0.0, result2.x, tolerance)
        assertEquals(1.0, result2.y, tolerance)
    }

    /**
     * Show sling function.
     *
     * sling(r, 1) traces a circle of radius r once as t goes 0 to 1.
     */
    @Test
    fun `show sling function`() {
        val sling = Sling(ConstPoint(0.0, 1.0), ConstPoint(0.0, 1.0))

        // At t=0, angle=0, point=(1, 0)
        val r0 = sling.f(0.0)
        assertEquals(1.0, r0.x, tolerance)
        assertEquals(0.0, r0.y, tolerance)

        // At t=0.25, angle=pi/2, point=(0, 1)
        val r25 = sling.f(0.25)
        assertEquals(0.0, r25.x, tolerance)
        assertEquals(1.0, r25.y, tolerance)

        // At t=0.5, angle=pi, point=(-1, 0)
        val r50 = sling.f(0.5)
        assertEquals(-1.0, r50.x, tolerance)
        assertEquals(0.0, r50.y, tolerance)
    }

    /**
     * Show cartesian coordinates.
     */
    @Test
    fun `show cartesian coordinates`() {
        // cartesian(t, 1-t) creates a diagonal line
        val cart = Cartesian(T(), Arithmetic('-', ConstPoint(0.0, 1.0), T()))

        val r0 = cart.f(0.0)
        assertEquals(0.0, r0.x, tolerance)
        assertEquals(1.0, r0.y, tolerance)

        val r1 = cart.f(1.0)
        assertEquals(1.0, r1.x, tolerance)
        assertEquals(0.0, r1.y, tolerance)
    }

    /**
     * Show variable usage.
     */
    @Test
    fun `show variable`() {
        val r = Variable("r")
        r.setValue(ConstPoint(0.0, 2.0))

        val sling = Sling(r, ConstPoint(0.0, 1.0))
        val evaluated = sling.eval()

        // After evaluation, r should be replaced with its value
        val result = evaluated.f(0.0)
        assertEquals(2.0, result.x, tolerance) // radius 2 at angle 0
        assertEquals(0.0, result.y, tolerance)
    }

    /**
     * Show extrema calculation.
     */
    @Test
    fun `show extrema`() {
        val sling = Sling(ConstPoint(0.0, 1.0), ConstPoint(0.0, 1.0))
        val extrema = sling.extrema(100)

        assertEquals(-1.0, extrema.min.x, 0.1)
        assertEquals(-1.0, extrema.min.y, 0.1)
        assertEquals(1.0, extrema.max.x, 0.1)
        assertEquals(1.0, extrema.max.y, 0.1)
    }

    /**
     * Show function composition.
     */
    @Test
    fun `show function composition`() {
        // sin(2*pi*t) - creates a sine wave
        val twoPI = Arithmetic('*', ConstPoint(0.0, 2.0 * PI), T())
        val sineWave = Sin(twoPI)

        val r0 = sineWave.f(0.0)
        assertEquals(0.0, r0.y, tolerance)

        val r25 = sineWave.f(0.25)
        assertEquals(1.0, r25.y, tolerance)

        val r75 = sineWave.f(0.75)
        assertEquals(-1.0, r75.y, tolerance)
    }

    /**
     * Show abs function.
     */
    @Test
    fun `show abs function`() {
        val absT = Abs(Arithmetic('-', T(), ConstPoint(0.0, 0.5)))

        // |0 - 0.5| = 0.5
        assertEquals(0.5, absT.f(0.0).y, tolerance)
        // |0.5 - 0.5| = 0
        assertEquals(0.0, absT.f(0.5).y, tolerance)
        // |1 - 0.5| = 0.5
        assertEquals(0.5, absT.f(1.0).y, tolerance)
    }

    /**
     * Show floor function.
     */
    @Test
    fun `show floor function`() {
        val scale = Arithmetic('*', T(), ConstPoint(0.0, 5.0))
        val floorT = Floor(scale)

        assertEquals(0.0, floorT.f(0.0).y, tolerance)
        assertEquals(2.0, floorT.f(0.5).y, tolerance)
        assertEquals(4.0, floorT.f(0.99).y, tolerance)
    }

    /**
     * Show ceil function.
     */
    @Test
    fun `show ceil function`() {
        val scale = Arithmetic('*', T(), ConstPoint(0.0, 5.0))
        val ceilT = Ceil(scale)

        assertEquals(0.0, ceilT.f(0.0).y, tolerance)
        assertEquals(3.0, ceilT.f(0.5).y, tolerance)
        assertEquals(5.0, ceilT.f(0.99).y, tolerance)
    }

    /**
     * Show function toString.
     */
    @Test
    fun `show function toString`() {
        val t = T()
        assertEquals("t", t.toString())

        val sinT = Sin(t)
        assertEquals("sin(t)", sinT.toString())

        val sum = Arithmetic('+', t, t)
        assertEquals("+(t, t)", sum.toString())

        val sling = Sling(ConstPoint(0.0, 1.0), t)
        assertTrue(sling.toString().contains("sling"))
    }

    /**
     * Note: The full Sling DSL includes:
     * - SlingParser: A parser that builds functions from text like "sling(1, 1)"
     * - Slider support (s1, s2): Interactive variables for live adjustment
     * - UI components (SlingIde, SlingPanel): Visualization of the plots
     *
     * These are omitted as they require UI frameworks.
     */
}
