package net.codenest.kassemblix.examples.logic

import net.codenest.kassemblix.examples.engine.*
import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import net.codenest.kassemblix.examples.track.KTrack
import net.codenest.kassemblix.examples.mechanics.KLowercaseWord
import net.codenest.kassemblix.examples.mechanics.KUppercaseWord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Logic (Logikus) examples demonstrating a Prolog-like logic language.
 *
 * Converted from: com.sjm.examples.logic.*
 *
 * The grammar this class supports is:
 * ```
 *     axiom        = structure (ruleDef | Empty);
 *     structure    = functor ('(' commaList(term) ')' | Empty);
 *     functor      = '.' | LowercaseWord | QuotedString;
 *     term         = structure | Num | list | variable;
 *     variable     = UppercaseWord | '_';
 *
 *     ruleDef      = ":-" commaList(condition);
 *     condition    = structure | not | evaluation | comparison | list;
 *
 *     not          = "not" structure ;
 *
 *     evaluation   = '#' '(' arg ',' arg ')';
 *     comparison   = operator '(' arg ',' arg ')';
 *     arg          = expression | functor;
 *     operator     = '<' | '>' | '=' | "<=" | ">=" | "!=" ;
 *     expression   = phrase ('+' phrase | '-' phrase)*;
 *     phrase       = factor ('*' factor | '/' factor)*;
 *     factor       = '(' expression ')' | Num | variable;
 *
 *     list         = '[' (listContents | Empty) ']';
 *     listContents = commaList(term) listTail;
 *     listTail     = ('|' (variable | list)) | Empty;
 *
 *     commaList(p) = p (',' p)*;
 * ```
 *
 * @author Steven J. Metsker
 */

// ============================================================
// Exception
// ============================================================

/**
 * Signals a problem parsing Logikus program or query.
 */
class LogikusException(message: String) : RuntimeException(message)

// ============================================================
// Assemblers
// ============================================================

/**
 * Exchanges a token on an assembly's stack with an atom
 * that has the token's value as its functor.
 */
class AtomAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val t = assembly.pop() as KToken
        if (t.isQuotedString()) {
            val s = t.sval ?: ""
            val plain = s.substring(1, s.length - 1)
            assembly.push(Atom(plain))
        } else if (t.isNumber()) {
            assembly.push(NumberFact(t.nval))
        } else {
            assembly.push(Atom(t.value() ?: ""))
        }
    }
}

/**
 * Pops a string like "X" from an assembly's stack and pushes a variable.
 */
class VariableAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val t = assembly.pop() as KToken
        val name = t.sval ?: ""
        assembly.push(Variable(name))
    }
}

/**
 * Pushes an anonymous variable onto an assembly's stack.
 */
class AnonymousAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        assembly.push(Anonymous())
    }
}

/**
 * Pops the terms and functor of a structure from an assembly's stack,
 * builds a structure, and pushes it.
 */
class StructureWithTermsAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val termList = elementsAbove(assembly, KToken.createSymbol("("))
        val termArray = listReversedIntoTerms(termList)
        val t = assembly.pop() as KToken
        assembly.push(Structure(t.value() ?: "", termArray))
    }

    companion object {
        fun listReversedIntoTerms(list: List<Any>): Array<Term> {
            return list.reversed().map { it as Term }.toTypedArray()
        }
    }
}

/**
 * Pops all of the structures on the stack, builds a rule, and pushes it.
 */
class AxiomAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val stack = assembly.getStack()
        val structures = stack.map { it as Structure }.toTypedArray()
        // Clear the stack by popping all elements
        repeat(stack.size) { assembly.pop() }
        assembly.push(Rule(structures))
    }
}

/**
 * Pops two comparison terms and an operator, builds the comparison, and pushes it.
 */
class ComparisonAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val second = assembly.pop() as ComparisonTerm
        val first = assembly.pop() as ComparisonTerm
        val t = assembly.pop() as KToken
        assembly.push(Comparison(t.sval ?: "", first, second))
    }
}

/**
 * Pops two arithmetic operands, builds an ArithmeticOperator, and pushes it.
 */
class ArithmeticAssembler(private val operator: Char) : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val operand1 = assembly.pop() as ArithmeticTerm
        val operand0 = assembly.pop() as ArithmeticTerm
        assembly.push(ArithmeticOperator(operator, operand0, operand1))
    }
}

/**
 * Pops two terms, constructs an Evaluation, and pushes it.
 */
class EvaluationAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val second = assembly.pop() as Term
        val first = assembly.pop() as Term
        assembly.push(Evaluation(first, second))
    }
}

/**
 * Pops a structure from the stack and pushes a Not version of it.
 */
class NotAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val s = assembly.pop() as Structure
        assembly.push(Not(s))
    }
}

/**
 * Pops the terms of a list from an assembly's stack, builds the list, and pushes it.
 */
class ListAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val fence = KToken.createSymbol("[")
        val termList = elementsAbove(assembly, fence)
        val termArray = StructureWithTermsAssembler.listReversedIntoTerms(termList)

        if (termArray.isEmpty()) {
            assembly.push(EmptyList)
        } else {
            assembly.push(Structure.list(termArray))
        }
    }
}

/**
 * Pops the tail and terms of a list from an assembly's stack,
 * builds the list, and pushes it.
 */
class ListWithTailAssembler : KTokenAssembler() {
    override fun workOn(assembly: KAssembly<KToken>) {
        val tail = assembly.pop() as Term
        val fence = KToken.createSymbol("[")
        val termList = elementsAbove(assembly, fence)
        val termsToLast = StructureWithTermsAssembler.listReversedIntoTerms(termList)
        assembly.push(Structure.list(termsToLast, tail))
    }
}

// ============================================================
// Parser
// ============================================================

/**
 * Parser for the Logikus language.
 */
class LogikusParser {
    private var structure: KSequence<KToken>? = null
    private var expression: KSequence<KToken>? = null
    private var list: KTrack<KToken>? = null

    /**
     * arg = expression | functor;
     */
    protected fun arg(): KParser<KToken> {
        return KAlternation<KToken>()
            .add(expression())
            .add(functor().setAssembler(AtomAssembler()))
    }

    /**
     * axiom = structure (ruleDef | Empty);
     */
    fun axiom(): KParser<KToken> {
        val s = KSequence<KToken>("axiom")
        s.add(structure())

        val a = KAlternation<KToken>()
            .add(ruleDef())
            .add(KEmpty())

        s.add(a)
        s.setAssembler(AxiomAssembler())
        return s
    }

    /**
     * commaList(p) = p (',' p)*;
     */
    protected fun commaList(p: KParser<KToken>): KSequence<KToken> {
        val commaP = KTrack<KToken>()
            .add(KSymbol(',').discard())
            .add(p)

        return KSequence<KToken>()
            .add(p)
            .add(KRepetition(subParser = commaP))
    }

    /**
     * comparison = operator '(' arg ',' arg ')';
     */
    fun comparison(): KParser<KToken> {
        return KTrack<KToken>("comparison")
            .add(operator())
            .add(KSymbol('(').discard())
            .add(arg())
            .add(KSymbol(',').discard())
            .add(arg())
            .add(KSymbol(')').discard())
            .also { it.setAssembler(ComparisonAssembler()) }
    }

    /**
     * condition = structure | not | evaluation | comparison | list;
     */
    fun condition(): KParser<KToken> {
        return KAlternation<KToken>("condition")
            .add(structure())
            .add(not())
            .add(evaluation())
            .add(comparison())
            .add(list())
    }

    /**
     * divideFactor = '/' factor;
     */
    protected fun divideFactor(): KParser<KToken> {
        return KSequence<KToken>("divideFactor")
            .add(KSymbol('/').discard())
            .add(factor())
            .also { it.setAssembler(ArithmeticAssembler('/')) }
    }

    /**
     * evaluation = '#' '(' arg ',' arg ')';
     */
    protected fun evaluation(): KParser<KToken> {
        return KTrack<KToken>("evaluation")
            .add(KSymbol('#').discard())
            .add(KSymbol('(').discard())
            .add(arg())
            .add(KSymbol(',').discard())
            .add(arg())
            .add(KSymbol(')').discard())
            .also { it.setAssembler(EvaluationAssembler()) }
    }

    /**
     * expression = phrase ('+' phrase | '-' phrase)*;
     */
    protected fun expression(): KParser<KToken> {
        if (expression == null) {
            expression = KSequence("expression")
            expression!!.add(phrase())
            val a = KAlternation<KToken>()
                .add(plusPhrase())
                .add(minusPhrase())
            expression!!.add(KRepetition(subParser = a))
        }
        return expression!!
    }

    /**
     * factor = '(' expression ')' | Num | variable;
     */
    protected fun factor(): KParser<KToken> {
        val a = KAlternation<KToken>("factor")

        val s = KSequence<KToken>()
            .add(KSymbol('(').discard())
            .add(expression())
            .add(KSymbol(')').discard())

        a.add(s)
        a.add(num())
        a.add(variable())
        return a
    }

    /**
     * functor = '.' | LowercaseWord | QuotedString;
     */
    protected fun functor(): KParser<KToken> {
        return KAlternation<KToken>("functor")
            .add(KSymbol('.'))
            .add(KLowercaseWord())
            .add(KQuotedString())
    }

    /**
     * list = '[' (listContents | Empty) ']';
     */
    fun list(): KParser<KToken> {
        if (list == null) {
            list = KTrack("list")
            list!!.add(KSymbol('['))  // push this as a fence

            val a = KAlternation<KToken>()
                .add(listContents())
                .add(KEmpty<KToken>().setAssembler(ListAssembler()))

            list!!.add(a)
            list!!.add(KSymbol(']').discard())
        }
        return list!!
    }

    /**
     * listContents = commaList(term) listTail;
     */
    protected fun listContents(): KParser<KToken> {
        return commaList(term()).add(listTail())
    }

    /**
     * listTail = ('|' (variable | list)) | Empty;
     */
    protected fun listTail(): KParser<KToken> {
        val tail = KAlternation<KToken>()
            .add(variable())
            .add(list())

        val barTail = KTrack<KToken>("bar tail")
            .add(KSymbol('|').discard())
            .add(tail)
            .also { it.setAssembler(ListWithTailAssembler()) }

        return KAlternation<KToken>()
            .add(barTail)
            .add(KEmpty<KToken>().setAssembler(ListWithTailAssembler()))
    }

    /**
     * minusPhrase = '-' phrase;
     */
    protected fun minusPhrase(): KParser<KToken> {
        return KSequence<KToken>("minusPhrase")
            .add(KSymbol('-').discard())
            .add(phrase())
            .also { it.setAssembler(ArithmeticAssembler('-')) }
    }

    /**
     * not = "not" structure;
     */
    protected fun not(): KParser<KToken> {
        return KTrack<KToken>("not")
            .add(KLiteral("not").discard())
            .add(structure())
            .also { it.setAssembler(NotAssembler()) }
    }

    /**
     * Returns a parser that recognizes a number and stacks an atom.
     */
    fun num(): KParser<KToken> {
        return KNum().setAssembler(AtomAssembler())
    }

    /**
     * operator = '<' | '>' | '=' | "<=" | ">=" | "!=" ;
     */
    protected fun operator(): KParser<KToken> {
        return KAlternation<KToken>("operator")
            .add(KSymbol('<'))
            .add(KSymbol('>'))
            .add(KSymbol('='))
            .add(KSymbol("<="))
            .add(KSymbol(">="))
            .add(KSymbol("!="))
    }

    /**
     * phrase = factor ('*' factor | '/' factor)*;
     */
    protected fun phrase(): KParser<KToken> {
        return KSequence<KToken>("phrase")
            .add(factor())
            .add(KRepetition(subParser = KAlternation<KToken>()
                .add(timesFactor())
                .add(divideFactor())))
    }

    /**
     * plusPhrase = '+' phrase;
     */
    protected fun plusPhrase(): KParser<KToken> {
        return KSequence<KToken>("plusPhrase")
            .add(KSymbol('+').discard())
            .add(phrase())
            .also { it.setAssembler(ArithmeticAssembler('+')) }
    }

    /**
     * ruleDef = ":-" commaList(condition);
     */
    protected fun ruleDef(): KParser<KToken> {
        return KTrack<KToken>("rule definition")
            .add(KSymbol(":-").discard())
            .add(commaList(condition()))
    }

    /**
     * structure = functor ('(' commaList(term) ')' | Empty);
     */
    protected fun structure(): KParser<KToken> {
        if (structure == null) {
            structure = KSequence("structure")
            structure!!.add(functor())

            val t = KTrack<KToken>("list in parens")
                .add(KSymbol('('))  // push this as a fence
                .add(commaList(term()))
                .add(KSymbol(')').discard())
                .also { it.setAssembler(StructureWithTermsAssembler()) }

            val a = KAlternation<KToken>()
                .add(t)
                .add(KEmpty<KToken>().setAssembler(AtomAssembler()))

            structure!!.add(a)
        }
        return structure!!
    }

    /**
     * term = structure | Num | list | variable;
     */
    protected fun term(): KParser<KToken> {
        return KAlternation<KToken>("term")
            .add(structure())
            .add(num())
            .add(list())
            .add(variable())
    }

    /**
     * timesFactor = '*' factor;
     */
    protected fun timesFactor(): KParser<KToken> {
        return KSequence<KToken>("timesFactor")
            .add(KSymbol('*').discard())
            .add(factor())
            .also { it.setAssembler(ArithmeticAssembler('*')) }
    }

    /**
     * variable = UppercaseWord | '_';
     */
    protected fun variable(): KParser<KToken> {
        val v = KUppercaseWord().setAssembler(VariableAssembler())
        val anon = KSymbol('_').discard().setAssembler(AnonymousAssembler())

        return KAlternation<KToken>()
            .add(v)
            .add(anon)
    }

    companion object {
        /**
         * query = commaList(condition);
         */
        fun query(): KParser<KToken> {
            return LogikusParser().commaList(LogikusParser().condition())
                .also { it.setAssembler(AxiomAssembler()) }
        }

        /**
         * Entry point for parsing an axiom.
         */
        fun start(): KParser<KToken> {
            return LogikusParser().axiom()
        }
    }
}

// ============================================================
// Facade
// ============================================================

/**
 * Provides utility methods for parsing Logikus programs and queries.
 */
object LogikusFacade {

    /**
     * Translate one axiom string into an Axiom object.
     */
    fun axiom(s: String): Axiom {
        val p = LogikusParser().axiom()
        val o = parse(s, p, "axiom")
        return o as Axiom
    }

    private fun checkForUppercase(s: String, type: String) {
        val trimmed = s.trim()
        if (trimmed.isNotEmpty() && trimmed[0].isUpperCase()) {
            throw LogikusException(
                "> Uppercase ${trimmed.takeWhile { it.isLetterOrDigit() }} indicates a variable and cannot begin a $type.\n"
            )
        }
    }

    private fun parse(s: String, p: KParser<KToken>, type: String): Any {
        val ta = KTokenAssembly(s)
        val out = p.bestMatch(ta)
        if (out == null) {
            checkForUppercase(s, type)
            throw LogikusException("> Cannot parse $type: $s\n")
        }
        if (out.hasMoreItem()) {
            val remainder = out.remainItems()
            if (remainder != ";") {
                throw LogikusException(
                    "> Input for $type appears complete after : \n> ${out.consumedItems()}\n"
                )
            }
        }
        return out.pop()!!
    }

    /**
     * Parse the text of a Logikus program and return a Program object.
     */
    fun program(s: String): Program {
        val p = Program()
        val lines = s.split(";").filter { it.isNotBlank() }
        for (line in lines) {
            p.addAxiom(axiom(line.trim()))
        }
        return p
    }

    /**
     * Parse the text of a Logikus query and return a Query object.
     */
    fun query(s: String, axiomSource: AxiomSource): Query {
        val o = parse(s, LogikusParser.query(), "query")
        return when (o) {
            is Fact -> Query(axiomSource, o)
            is Rule -> Query(axiomSource, o)
            else -> throw LogikusException("Unexpected query result: $o")
        }
    }
}

// ============================================================
// Test class
// ============================================================

class LogicTest {

    /**
     * Show parsing a simple atom.
     */
    @Test
    fun `show parse atom`() {
        val axiom = LogikusFacade.axiom("hello")
        assertNotNull(axiom)
        assertTrue(axiom.toString().contains("hello"))
    }

    /**
     * Show parsing a structure with terms.
     */
    @Test
    fun `show parse structure`() {
        val axiom = LogikusFacade.axiom("city(denver, 5280)")
        assertNotNull(axiom)
        assertTrue(axiom.toString().contains("city"))
        assertTrue(axiom.toString().contains("denver"))
        assertTrue(axiom.toString().contains("5280"))
    }

    /**
     * Show parsing a structure with multiple terms.
     */
    @Test
    fun `show parse structure with multiple terms`() {
        val axiom = LogikusFacade.axiom("person(john, male, tall)")
        assertNotNull(axiom)
        assertTrue(axiom.toString().contains("person"))
        assertTrue(axiom.toString().contains("john"))
    }

    /**
     * Show parsing a program with simple facts.
     */
    @Test
    fun `show parse program`() {
        val programText = """
            city(denver, 5280);
            city(boston, 20);
            city(chicago, 600);
        """.trimIndent()

        val program = LogikusFacade.program(programText)
        assertNotNull(program)
    }

    /**
     * Show simple query against facts.
     */
    @Test
    fun `show simple query`() {
        val programText = """
            city(denver, 5280);
            city(boston, 20);
        """.trimIndent()

        val program = LogikusFacade.program(programText)
        val query = LogikusFacade.query("city(denver, 5280)", program)

        assertTrue(query.canFindNextProof())
    }

    /**
     * Show query with variable.
     */
    @Test
    fun `show query with variable`() {
        val programText = """
            city(denver, 5280);
            city(boston, 20);
            city(chicago, 600);
        """.trimIndent()

        val program = LogikusFacade.program(programText)
        val query = LogikusFacade.query("city(Name, Alt)", program)

        val cities = mutableListOf<String>()
        while (query.canFindNextProof()) {
            cities.add(query.variables().toString())
        }

        assertEquals(3, cities.size)
    }

    /**
     * Show parsing an empty list.
     */
    @Test
    fun `show parse empty list`() {
        val axiom = LogikusFacade.axiom("empty([])")
        assertNotNull(axiom)
        assertTrue(axiom.toString().contains("empty"))
    }

    /**
     * Show using the engine directly with Comparison.
     * This tests the engine without going through the parser.
     */
    @Test
    fun `show engine comparison directly`() {
        val p = Program()
        p.addAxiom(Fact("city", "denver", 5280))
        p.addAxiom(Fact("city", "boston", 20))
        p.addAxiom(Fact("city", "flagstaff", 6970))

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

        // Should find denver (5280) and flagstaff (6970)
        assertEquals(2, highCities.size)
        assertTrue(highCities.contains("denver"))
        assertTrue(highCities.contains("flagstaff"))
    }

    /**
     * Show using the engine directly with Not.
     */
    @Test
    fun `show engine not directly`() {
        val p = Program()

        // bachelor(X) :- male(X), not married(X)
        val x = Variable("X")
        val r = Rule(arrayOf(
            Structure("bachelor", arrayOf<Term>(x)),
            Structure("male", arrayOf<Term>(x)),
            Not("married", arrayOf<Term>(x))
        ))
        p.addAxiom(r)
        p.addAxiom(Fact("married", "jim"))
        p.addAxiom(Fact("male", "jeremy"))
        p.addAxiom(Fact("male", "jim"))

        val b = Variable("B")
        val q = Query(p, Structure("bachelor", arrayOf<Term>(b)))

        val bachelors = mutableListOf<String>()
        while (q.canFindNextProof()) {
            bachelors.add(b.toString())
        }

        assertEquals(1, bachelors.size)
        assertEquals("jeremy", bachelors[0])
    }

    /**
     * Show using the engine directly with lists.
     */
    @Test
    fun `show engine list directly`() {
        val p = Program()

        // member(X, [X | Rest])
        val x = Variable("X")
        val rest = Variable("Rest")
        val memberBase = Rule(arrayOf(
            Structure("member", arrayOf(x, Structure.list(arrayOf(x), rest)))
        ))
        p.addAxiom(memberBase)

        // member(X, [Y | Rest]) :- member(X, Rest)
        val y = Variable("Y")
        val memberRec = Rule(arrayOf(
            Structure("member", arrayOf(x, Structure.list(arrayOf(y), rest))),
            Structure("member", arrayOf(x, rest))
        ))
        p.addAxiom(memberRec)

        val snakes = Structure.list(arrayOf<Any>("cobra", "garter", "python"))

        val elem = Variable("Elem")
        val q = Query(p, Structure("member", arrayOf(elem, snakes)))

        val members = mutableListOf<String>()
        while (q.canFindNextProof()) {
            members.add(elem.toString())
        }

        assertEquals(3, members.size)
        assertTrue(members.contains("cobra"))
        assertTrue(members.contains("garter"))
        assertTrue(members.contains("python"))
    }

    /**
     * Show using the engine directly with Evaluation.
     */
    @Test
    fun `show engine evaluation directly`() {
        val x = Variable("x")
        val y = Variable("y")
        val result = Variable("result")

        x.unify(NumberFact(5.0))
        y.unify(NumberFact(3.0))

        val sum = ArithmeticOperator('+', x, y)
        val e = Evaluation(result, sum)

        assertTrue(e.canFindNextProof())
        assertEquals(8.0, result.eval())
    }
}
