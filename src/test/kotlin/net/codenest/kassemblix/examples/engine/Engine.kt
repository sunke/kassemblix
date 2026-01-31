package net.codenest.kassemblix.examples.engine

/**
 * Logic Engine - A Prolog-like unification and proof engine.
 *
 * Converted from: com.sjm.engine.*
 *
 * This package provides:
 * - Terms (structures and variables) that can unify
 * - Rules and Facts that form programs
 * - Queries that prove themselves against programs
 * - Arithmetic and comparison operations
 *
 * @author Steven J. Metsker (original Java)
 */

// ============================================================
// Exceptions
// ============================================================

/**
 * Signals that an ArithmeticOperator could not be evaluated.
 * This happens when an evaluation refers to an uninstantiated variable.
 */
class EvaluationException(message: String? = null) : RuntimeException(message)

// ============================================================
// Core Interfaces
// ============================================================

/**
 * The Term interface defines the core elements of the logic engine.
 *
 * Terms are the central objects in the logic programming data model:
 * - A Program is a collection of Rules.
 * - A Rule is a series of Structures.
 * - A Structure is an Object associated with a collection of Terms.
 * - Structures and Variables are Terms.
 */
interface Term {
    /**
     * Returns a copy of the term for use in a proof.
     */
    fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term

    /**
     * The value that this term should present to an evaluating function.
     */
    fun eval(): Any?

    /**
     * Return true if this term is a list.
     */
    fun isList(): Boolean

    /**
     * Returns a string representation of this listTailTerm.
     */
    fun listTailString(): String

    /**
     * Returns a collection of variables that allow this term to unify with a structure.
     */
    fun unify(s: Structure): Unification?

    /**
     * Returns a set of variable instantiations that allow two terms to unify.
     */
    fun unify(t: Term): Unification?

    /**
     * Returns a collection of variables that allow this term to unify with a variable.
     */
    fun unify(v: Variable): Unification?

    /**
     * Returns the variables associated with this term.
     */
    fun variables(): Unification
}

/**
 * Marker interface for terms that can participate in comparisons.
 */
interface ComparisonTerm : Term

/**
 * Marker interface for terms that can participate in arithmetic expressions.
 */
interface ArithmeticTerm : ComparisonTerm

/**
 * Marker interface for terms that evaluate to a Boolean.
 */
interface BooleanTerm : Term

// ============================================================
// Axiom Interfaces
// ============================================================

/**
 * An Axiom is either a fact or a rule that can appear in a program.
 */
interface Axiom {
    /**
     * Return an axiom that a consulting structure can use to prove itself.
     */
    fun dynamicAxiom(axiomSource: AxiomSource?): DynamicAxiom

    /**
     * Return the first structure of this axiom.
     */
    fun head(): Structure
}

/**
 * A DynamicAxiom is an axiom that a structure can consult to prove itself.
 */
interface DynamicAxiom {
    /**
     * Return the first structure of this dynamic axiom.
     */
    fun head(): Structure

    /**
     * Return the tail of this dynamic axiom.
     */
    fun resolvent(): DynamicRule
}

/**
 * An AxiomEnumeration generates a series of axioms, one at a time.
 */
interface AxiomEnumeration {
    /**
     * Tests if this enumeration contains more axioms.
     */
    fun hasMoreAxioms(): Boolean

    /**
     * Returns the next axiom of this enumeration.
     */
    fun nextAxiom(): Axiom
}

/**
 * An AxiomSource is a provider of axioms.
 */
interface AxiomSource {
    /**
     * Returns all the axioms from a source.
     */
    fun axioms(): AxiomEnumeration

    /**
     * Returns an enumeration of axioms for a specific structure.
     */
    fun axioms(s: Structure): AxiomEnumeration
}

// ============================================================
// Unification
// ============================================================

/**
 * A Unification is a collection of variables.
 *
 * Structures and variables use unifications to keep track of the
 * variable assignments that make a proof work.
 */
class Unification(v: Variable? = null) {
    private var list: MutableList<Variable>? = null

    init {
        if (v != null) {
            addVariable(v)
        }
    }

    /**
     * Adds a variable to this unification.
     */
    fun addVariable(v: Variable): Unification {
        if (!getList().contains(v)) {
            list!!.add(v)
        }
        return this
    }

    /**
     * Adds all the variables of another unification to this one.
     */
    fun append(u: Unification): Unification {
        for (i in 0 until u.size()) {
            addVariable(u.variableAt(i))
        }
        return this
    }

    /**
     * Return the variables in this unification.
     */
    fun elements(): Iterator<Variable> = getList().iterator()

    /**
     * Returns the number of variables in this unification.
     */
    fun size(): Int = list?.size ?: 0

    /**
     * Returns a string representation of this unification.
     */
    override fun toString(): String {
        return (0 until size()).joinToString(", ") { variableAt(it).definitionString() }
    }

    /**
     * Returns a string representation without variable names.
     */
    fun toStringQuiet(): String {
        return (0 until size()).joinToString(", ") { variableAt(it).toString() }
    }

    /**
     * Asks all the contained variables to unbind.
     */
    fun unbind() {
        for (i in 0 until size()) {
            variableAt(i).unbind()
        }
    }

    /**
     * Returns the variable at the indicated index.
     */
    internal fun variableAt(i: Int): Variable = getList()[i]

    private fun getList(): MutableList<Variable> {
        if (list == null) {
            list = mutableListOf()
        }
        return list!!
    }

    companion object {
        val empty = Unification()
    }
}

// ============================================================
// Scope
// ============================================================

/**
 * A scope is a repository for variables. A dynamic rule has
 * a scope, which means that variables with the same name
 * are the same variable.
 */
class Scope(terms: Array<Term>? = null) : Cloneable {
    private var dictionary = mutableMapOf<String, Variable>()

    init {
        if (terms != null) {
            for (term in terms) {
                val u = term.variables()
                val e = u.elements()
                while (e.hasNext()) {
                    val v = e.next()
                    dictionary[v.name] = v
                }
            }
        }
    }

    /**
     * Remove all variables from this scope.
     */
    fun clear() {
        dictionary.clear()
    }

    /**
     * Return a copy of this object.
     */
    public override fun clone(): Scope {
        val clone = Scope()
        clone.dictionary = dictionary.toMutableMap()
        return clone
    }

    /**
     * Returns true if a variable of the given name appears in this scope.
     */
    fun isDefined(name: String): Boolean = dictionary.containsKey(name)

    /**
     * Returns a variable of the given name from this scope.
     * If not already in scope, creates and adds the variable.
     */
    fun lookup(name: String): Variable {
        return dictionary.getOrPut(name) { Variable(name) }
    }
}

// ============================================================
// Variable
// ============================================================

/**
 * A variable is a named term that can unify with other terms.
 *
 * A variable has a name and an instantiation. When a variable
 * unifies with a term, it "instantiates" to it.
 */
open class Variable(val name: String) : ArithmeticTerm, ComparisonTerm {
    protected var instantiation: Term? = null

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        return scope.lookup(name)
    }

    /**
     * Returns string representation showing both name and value.
     */
    fun definitionString(): String {
        return if (instantiation != null) {
            "$name = $instantiation"
        } else {
            name
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Variable) return false
        if (name != other.name) return false
        if (instantiation == null) {
            return other.instantiation == null
        }
        return instantiation == other.instantiation
    }

    override fun hashCode(): Int = name.hashCode()

    override fun eval(): Any? {
        if (instantiation == null) {
            throw EvaluationException("Variable $name is undefined")
        }
        return instantiation!!.eval()
    }

    override fun isList(): Boolean {
        return instantiation?.isList() ?: true
    }

    override fun listTailString(): String {
        return instantiation?.listTailString() ?: "|$name"
    }

    override fun toString(): String {
        return instantiation?.toString() ?: name
    }

    /**
     * Marks this variable as no longer having an instantiated value.
     */
    fun unbind() {
        instantiation = null
    }

    override fun unify(s: Structure): Unification? {
        if (instantiation != null) {
            return instantiation!!.unify(s)
        }
        instantiation = s
        return Unification(this)
    }

    override fun unify(t: Term): Unification? {
        return t.unify(this)
    }

    override fun unify(v: Variable): Unification? {
        if (this === v) {
            return Unification()
        }
        if (instantiation != null) {
            return instantiation!!.unify(v)
        }
        if (v.instantiation != null) {
            return v.instantiation!!.unify(this)
        }
        instantiation = v
        return Unification(this)
    }

    override fun variables(): Unification = Unification(this)
}

/**
 * An anonymous variable unifies successfully with any other term,
 * without binding to the term.
 */
class Anonymous : Variable("_") {
    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term = this

    override fun eval(): Any? = name

    override fun unify(s: Structure): Unification? = Unification.empty

    override fun unify(t: Term): Unification? = Unification.empty

    override fun unify(v: Variable): Unification? = Unification.empty

    override fun variables(): Unification = Unification.empty
}

// ============================================================
// Structure
// ============================================================

/**
 * A Structure is a functor associated with a number of terms.
 *
 * A functor can be any object. A term is an object that implements
 * the Term interface, including structures and variables.
 */
open class Structure(
    open val functor: Any,
    open val terms: Array<Term> = emptyArray()
) : Term {

    constructor(functor: Any) : this(functor, emptyArray())

    /**
     * Return the number of terms in this structure.
     */
    fun arity(): Int = terms.size

    /**
     * Returns false. Subclasses implement more interesting behavior.
     */
    open fun canFindNextProof(): Boolean = false

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        val newTerms = terms.map { it.copyForProof(axiomSource, scope) }.toTypedArray()
        return ConsultingStructure(axiomSource!!, functor, newTerms)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Structure) return false
        if (!functorAndArityEquals(other)) return false
        for (i in terms.indices) {
            if (terms[i] != other.terms[i]) return false
        }
        return true
    }

    override fun hashCode(): Int = functor.hashCode() + terms.contentHashCode()

    override fun eval(): Any? {
        return if (terms.isNotEmpty()) this else functor
    }

    /**
     * Returns true if this structure's functor and number of terms
     * match the supplied structure.
     */
    fun functorAndArityEquals(s: Structure): Boolean {
        return arity() == s.arity() && functor == s.functor
    }

    override fun isList(): Boolean {
        return terms.size == 2 && functor == "." && terms[1].isList()
    }

    override fun listTailString(): String {
        return ", " + listTermsToString()
    }

    protected open fun listTermsToString(): String {
        var s = terms[0].toString()
        if (terms.size > 1) {
            s += terms[1].listTailString()
        }
        return s
    }

    override fun toString(): String {
        if (isList()) {
            return "[" + listTermsToString() + "]"
        }
        val buf = StringBuilder(functor.toString())
        if (terms.isNotEmpty()) {
            buf.append("(")
            buf.append(terms.joinToString(", "))
            buf.append(")")
        }
        return buf.toString()
    }

    override fun unify(s: Structure): Unification? {
        if (!functorAndArityEquals(s)) {
            return null
        }
        val u = Unification()
        val others = s.terms
        for (i in terms.indices) {
            val subUnification = terms[i].unify(others[i])
            if (subUnification == null) {
                u.unbind()
                return null
            }
            u.append(subUnification)
        }
        return u
    }

    override fun unify(t: Term): Unification? = t.unify(this)

    override fun unify(v: Variable): Unification? = v.unify(this)

    override fun variables(): Unification {
        val u = Unification()
        for (term in terms) {
            u.append(term.variables())
        }
        return u
    }

    companion object {
        val emptyList = EmptyList

        /**
         * Helper method for list construction.
         */
        private fun headAndTail(terms: Array<Term>, tail: Term): Array<Term> {
            require(terms.isNotEmpty()) { "Cannot create a list with no head" }

            val headAndTail = arrayOfNulls<Term>(2)
            headAndTail[0] = terms[0]

            if (terms.size == 1) {
                headAndTail[1] = tail
            } else {
                val rest = terms.copyOfRange(1, terms.size)
                headAndTail[1] = list(rest, tail)
            }

            @Suppress("UNCHECKED_CAST")
            return headAndTail as Array<Term>
        }

        /**
         * Constructs a list from objects (wrapped as Facts).
         */
        fun list(objects: Array<Any>): Structure {
            @Suppress("UNCHECKED_CAST")
            return list(Fact.facts(objects) as Array<Term>)
        }

        /**
         * Constructs a list from the given terms.
         */
        fun list(terms: Array<Term>): Structure {
            return Structure(".", headAndTail(terms, emptyList))
        }

        /**
         * Constructs a list that terminates with a known list or variable.
         */
        fun list(terms: Array<Term>, tail: Term): Structure {
            return Structure(".", headAndTail(terms, tail))
        }
    }
}

// ============================================================
// Fact and its subclasses
// ============================================================

/**
 * A Fact is a Structure that contains only other Facts.
 * Since they do not contain variables, Facts do not need to copy themselves.
 */
open class Fact : Structure, Axiom, DynamicAxiom {

    constructor(functor: Any) : super(functor, emptyArray<Term>())

    @Suppress("UNCHECKED_CAST")
    constructor(functor: Any, objects: Array<Any>) : super(functor, facts(objects) as Array<Term>)

    constructor(functor: Any, terms: Array<Fact>) : super(functor, terms as Array<Term>)

    constructor(functor: Any, o: Any) : this(functor, arrayOf(o))

    constructor(functor: Any, o1: Any, o2: Any) : this(functor, arrayOf(o1, o2))

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term = this

    override fun dynamicAxiom(axiomSource: AxiomSource?): DynamicAxiom = this

    override fun head(): Structure = this

    override fun resolvent(): DynamicRule = Companion.resolvent

    /**
     * A speedier version of unify for facts.
     */
    fun unify(f: Fact): Unification? {
        if (!functorAndArityEquals(f)) {
            return null
        }
        for (i in terms.indices) {
            val f1 = terms[i] as Fact
            val f2 = f.terms[i] as Fact
            if (f1.unify(f2) == null) {
                return null
            }
        }
        return Unification.empty
    }

    companion object {
        internal val resolvent = DynamicRule(null, null, emptyArray())

        /**
         * Create an array of (atomic) facts from an array of objects.
         */
        fun facts(objects: Array<Any>): Array<Fact> {
            return objects.map { Atom(it) }.toTypedArray()
        }
    }
}

/**
 * An Atom is a Structure with no terms.
 */
open class Atom(functor: Any) : Fact(functor), ComparisonTerm {
    override fun eval(): Any = functor
}

/**
 * A NumberFact is a fact with a Number as its functor.
 */
class NumberFact : Atom, ArithmeticTerm {
    constructor(d: Double) : super(d)
    constructor(n: Number) : super(n)
}

/**
 * A BooleanFact is a fact with a Boolean as its functor.
 */
class BooleanFact : Atom {
    constructor(b: Boolean) : super(b)
}

/**
 * The EmptyList is a list with no terms.
 * All lists except this one contain a head and a tail.
 */
object EmptyList : Fact(".") {
    override fun isList(): Boolean = true

    override fun listTailString(): String = ""

    override fun toString(): String = "[]"
}

// ============================================================
// Program
// ============================================================

/**
 * A Program is a collection of rules and facts that together
 * form a logical model.
 */
class Program(axioms: Array<Axiom>? = null) : AxiomSource {
    internal val axiomList = mutableListOf<Axiom>()

    init {
        axioms?.forEach { addAxiom(it) }
    }

    /**
     * Adds an axiom to this program.
     */
    fun addAxiom(a: Axiom) {
        axiomList.add(a)
    }

    /**
     * Appends all the axioms of another source to this one.
     */
    fun append(source: AxiomSource) {
        val e = source.axioms()
        while (e.hasMoreAxioms()) {
            addAxiom(e.nextAxiom())
        }
    }

    override fun axioms(): AxiomEnumeration = ProgramEnumerator(this)

    override fun axioms(s: Structure): AxiomEnumeration = axioms()

    override fun toString(): String {
        return axiomList.joinToString("\n") { "$it;" }
    }
}

/**
 * A ProgramEnumerator returns the axioms of a program, one at a time.
 */
class ProgramEnumerator(program: Program) : AxiomEnumeration {
    private val iterator = program.axiomList.iterator()

    override fun hasMoreAxioms(): Boolean = iterator.hasNext()

    override fun nextAxiom(): Axiom = iterator.next()
}

// ============================================================
// Rule
// ============================================================

/**
 * A Rule represents a logic statement that a structure is true
 * if a following series of other structures are true.
 */
open class Rule(internal val structures: Array<Structure>) : Axiom {

    constructor(s: Structure) : this(arrayOf(s))

    override fun dynamicAxiom(axiomSource: AxiomSource?): DynamicAxiom {
        return DynamicRule(axiomSource, Scope(), this)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Rule) return false
        if (structures.size != other.structures.size) return false
        for (i in structures.indices) {
            if (structures[i] != other.structures[i]) return false
        }
        return true
    }

    override fun hashCode(): Int = structures.contentHashCode()

    override fun head(): Structure = structures[0]

    override fun toString(): String {
        val buf = StringBuilder()
        for (i in structures.indices) {
            when (i) {
                1 -> buf.append(" :- ")
                in 2..Int.MAX_VALUE -> buf.append(", ")
            }
            buf.append(structures[i].toString())
        }
        return buf.toString()
    }
}

// ============================================================
// DynamicRule
// ============================================================

/**
 * A DynamicRule represents a provable statement that a structure
 * is true if a following series of other structures are true.
 */
open class DynamicRule : Rule, DynamicAxiom {
    internal val axiomSource: AxiomSource?
    internal val scope: Scope?
    private var headInvolved = false
    private var tail: DynamicRule? = null

    internal constructor(
        axiomSource: AxiomSource?,
        scope: Scope?,
        structures: Array<Structure>
    ) : super(structures) {
        this.axiomSource = axiomSource
        this.scope = scope
    }

    internal constructor(
        axiomSource: AxiomSource?,
        scope: Scope?,
        rule: Rule
    ) : this(axiomSource, scope, provableStructures(axiomSource, scope!!, rule.structures))

    /**
     * "Can establish" means that either a rule can prove itself, or
     * that the rule is empty.
     */
    fun canEstablish(): Boolean {
        if (isEmpty()) {
            return true
        }
        return canFindNextProof()
    }

    /**
     * Tests if this rule can find another proof.
     */
    fun canFindNextProof(): Boolean {
        if (isEmpty()) {
            return false
        }
        if (headInvolved) {
            if (tail().canFindNextProof()) {
                return true
            }
        }
        while (true) {
            headInvolved = head().canFindNextProof()
            if (!headInvolved) {
                return false
            }
            if (tail().canEstablish()) {
                return true
            }
        }
    }

    /**
     * Return the home of this dynamic rule's variables.
     */
    fun getScope(): Scope? = scope

    /**
     * Return true if this rule contains no structures.
     */
    fun isEmpty(): Boolean = structures.isEmpty()

    /**
     * Return a variable of the given name.
     */
    fun lookup(name: String): Variable = scope!!.lookup(name)

    override fun resolvent(): DynamicRule = tail()

    /**
     * Returns the series of structures after the head.
     */
    fun tail(): DynamicRule {
        if (tail == null) {
            val rest = structures.copyOfRange(1, structures.size)
            tail = DynamicRule(axiomSource, scope, rest)
        }
        return tail!!
    }

    /**
     * Returns this executable rule's variables.
     */
    fun variables(): Unification {
        if (structures.isEmpty()) {
            return Unification.empty
        }
        return head().variables().append(tail().variables())
    }

    companion object {
        /**
         * Create provable versions of an input array of structures.
         */
        internal fun provableStructures(
            axiomSource: AxiomSource?,
            scope: Scope,
            structures: Array<Structure>
        ): Array<Structure> {
            return structures.map { s ->
                if (s is Fact) {
                    ConsultingStructure(axiomSource!!, s.functor, s.terms)
                } else {
                    s.copyForProof(axiomSource, scope) as Structure
                }
            }.toTypedArray()
        }
    }
}

// ============================================================
// Query
// ============================================================

/**
 * A Query is a dynamic rule that stands outside of a program
 * and proves itself by referring to a program.
 */
class Query : DynamicRule {

    constructor(axiomSource: AxiomSource?, structures: Array<Structure>)
        : this(axiomSource, Scope(structures as Array<Term>), structures)

    constructor(axiomSource: AxiomSource?, rule: Rule)
        : this(axiomSource, rule.structures)

    private constructor(axiomSource: AxiomSource?, scope: Scope, structures: Array<Structure>)
        : super(axiomSource, scope, provableStructures(axiomSource, scope, structures))

    constructor(axiomSource: AxiomSource?, structure: Structure)
        : this(axiomSource, arrayOf(structure))

    constructor(structure: Structure)
        : super(null, Scope(), arrayOf(structure))

    override fun toString(): String {
        return structures.joinToString(", ")
    }
}

// ============================================================
// Gateway
// ============================================================

/**
 * A Gateway is a structure that can prove its truth at most
 * once before failing.
 *
 * Examples of gateways are comparisons, negations, and
 * mathematical evaluations.
 */
abstract class Gateway(functor: Any, terms: Array<Term>) : Structure(functor, terms) {
    protected var open = false

    override fun canFindNextProof(): Boolean {
        if (open) {
            open = false
        } else {
            open = canProveOnce()
        }
        if (!open) {
            cleanup()
        }
        return open
    }

    /**
     * Returns true if this gateway can prove itself.
     */
    open fun canProveOnce(): Boolean = true

    /**
     * Cleanup after the gateway fails.
     */
    protected open fun cleanup() {}
}

// ============================================================
// ConsultingStructure
// ============================================================

/**
 * A ConsultingStructure is a structure that can prove itself
 * against an axiom source supplied with the constructor.
 */
class ConsultingStructure(
    private val source: AxiomSource,
    functor: Any,
    terms: Array<Term>
) : Structure(functor, terms) {

    internal var axioms: AxiomEnumeration? = null
    private var currentUnification: Unification? = null
    internal var resolvent: DynamicRule? = null

    private fun getAxioms(): AxiomEnumeration {
        if (axioms == null) {
            axioms = source.axioms(this)
        }
        return axioms!!
    }

    override fun canFindNextProof(): Boolean {
        if (resolvent != null) {
            if (resolvent!!.canFindNextProof()) {
                return true
            }
        }
        while (true) {
            Thread.yield()
            unbind()
            if (!canUnify()) {
                axioms = null
                return false
            }
            if (resolvent!!.canEstablish()) {
                return true
            }
        }
    }

    internal fun canUnify(): Boolean {
        while (getAxioms().hasMoreAxioms()) {
            val a = getAxioms().nextAxiom()
            val h = a.head()
            if (!functorAndArityEquals(h)) {
                continue
            }
            val aCopy = a.dynamicAxiom(source)

            currentUnification = aCopy.head().unify(this)
            resolvent = null
            if (currentUnification != null) {
                resolvent = aCopy.resolvent()
                return true
            }
        }
        return false
    }

    internal fun unbind() {
        currentUnification?.unbind()
        currentUnification = null
        resolvent = null
    }
}

// ============================================================
// ArithmeticOperator
// ============================================================

/**
 * An ArithmeticOperator represents an arithmetic operation
 * that will perform itself as part of a proof.
 */
class ArithmeticOperator(
    private val operator: Char,
    private val term0: ArithmeticTerm,
    private val term1: ArithmeticTerm
) : Structure(operator, arrayOf(term0, term1)), ArithmeticTerm {

    private fun arithmeticValue(d0: Double, d1: Double): Double {
        return when (operator) {
            '+' -> d0 + d1
            '-' -> d0 - d1
            '*' -> d0 * d1
            '/' -> d0 / d1
            '%' -> d0 % d1
            else -> 0.0
        }
    }

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        return ArithmeticOperator(
            operator,
            term0.copyForProof(null, scope) as ArithmeticTerm,
            term1.copyForProof(null, scope) as ArithmeticTerm
        )
    }

    override fun eval(): Any {
        val d0 = evalTerm(term0)
        val d1 = evalTerm(term1)
        return arithmeticValue(d0, d1)
    }

    private fun evalTerm(t: ArithmeticTerm): Double {
        val o = t.eval()
        if (o == null) {
            throw EvaluationException("$t is undefined in $this")
        }
        if (o !is Number) {
            throw EvaluationException("$t is not a number in $this")
        }
        return o.toDouble()
    }
}

// ============================================================
// Comparison
// ============================================================

/**
 * A Comparison object applies a comparison operator to its
 * terms in order to prove itself.
 */
class Comparison(
    private val operator: String,
    private val term0: ComparisonTerm,
    private val term1: ComparisonTerm
) : Gateway(operator, arrayOf(term0, term1)), BooleanTerm {

    override fun canProveOnce(): Boolean {
        val p0 = term0.eval()
        val p1 = term1.eval()
        return compare(p0, p1)
    }

    private fun compare(obj0: Any?, obj1: Any?): Boolean {
        if (obj0 is Number && obj1 is Number) {
            return compareNumber(obj0, obj1)
        }
        if (obj0 is String && obj1 is String) {
            return compareString(obj0, obj1)
        }
        return false
    }

    private fun compareNumber(n0: Number, n1: Number): Boolean {
        val d0 = n0.toDouble()
        val d1 = n1.toDouble()

        return when (operator) {
            ">" -> d0 > d1
            "<" -> d0 < d1
            "=" -> d0 == d1
            ">=" -> d0 >= d1
            "<=" -> d0 <= d1
            "!=" -> d0 != d1
            else -> false
        }
    }

    private fun compareString(s0: String, s1: String): Boolean {
        val comparison = s0.compareTo(s1)

        return when (operator) {
            ">" -> comparison > 0
            "<" -> comparison < 0
            "=" -> comparison == 0
            ">=" -> comparison >= 0
            "<=" -> comparison <= 0
            "!=" -> comparison != 0
            else -> false
        }
    }

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        return Comparison(
            operator,
            term0.copyForProof(null, scope) as ComparisonTerm,
            term1.copyForProof(null, scope) as ComparisonTerm
        )
    }

    override fun eval(): Any = canProveOnce()
}

// ============================================================
// Evaluation
// ============================================================

/**
 * An Evaluation unifies a term with the value of another term.
 */
class Evaluation(
    private val term0: Term,
    private val term1: Term
) : Gateway("#", arrayOf(term0, term1)) {

    private var currentUnification: Unification? = null

    override fun canProveOnce(): Boolean {
        val o: Any?
        try {
            o = term1.eval()
        } catch (e: EvaluationException) {
            return false
        }
        currentUnification = term0.unify(Atom(o!!))
        return currentUnification != null
    }

    override fun cleanup() {
        unbind()
    }

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        return Evaluation(
            term0.copyForProof(null, scope),
            term1.copyForProof(null, scope)
        )
    }

    private fun unbind() {
        currentUnification?.unbind()
        currentUnification = null
    }
}

// ============================================================
// Not
// ============================================================

/**
 * A Not is a structure that fails if it can prove itself
 * against a program.
 */
class Not : Structure {
    constructor(functor: Any) : super(functor, emptyArray())

    constructor(functor: Any, terms: Array<Term>) : super(functor, terms)

    constructor(s: Structure) : super(s.functor, s.terms)

    override fun copyForProof(axiomSource: AxiomSource?, scope: Scope): Term {
        val newTerms = terms.map { it.copyForProof(axiomSource, scope) }.toTypedArray()
        return ConsultingNot(ConsultingStructure(axiomSource!!, functor, newTerms))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Not) return false
        if (!functorAndArityEquals(other)) return false
        for (i in terms.indices) {
            if (terms[i] != other.terms[i]) return false
        }
        return true
    }

    override fun hashCode(): Int = super.hashCode()

    override fun toString(): String = "not ${super.toString()}"
}

/**
 * A ConsultingNot is a Not that has an axiom source to consult.
 */
class ConsultingNot(
    private val consultingStructure: ConsultingStructure
) : Gateway(consultingStructure.functor, consultingStructure.terms) {

    override fun canProveOnce(): Boolean {
        return !(consultingStructure.canUnify() &&
                 consultingStructure.resolvent!!.canEstablish())
    }

    override fun cleanup() {
        consultingStructure.unbind()
        consultingStructure.axioms = null
    }

    override fun toString(): String = "not $consultingStructure"
}
