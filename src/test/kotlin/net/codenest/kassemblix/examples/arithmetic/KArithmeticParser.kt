package net.codenest.kassemblix.example.arithmetic

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.*
import java.lang.Exception

/**
 * This class provides a parser that recognizes arithmetic expressions. It includes the method `value`,
 * which is a "facade" that makes the parser easy to use. For example,
 *
 * ```
 * System.out.println(ArithmeticParser.value("(5 + 4) * 3 ^ 2 - 81"));
 * ```
 * This prints out `0.0`.
 *
 * This parser recognizes expressions according to the following rules:
 *
 * ```
 * expression    = term (plusTerm | minusTerm)*;
 * term          = factor (timesFactor | divideFactor)*;
 * plusTerm      = '+' term;
 * minusTerm     = '-' term;
 * factor        = phrase expFactor | phrase;
 * timesFactor   = '*' factor;
 * divideFactor  = '/' factor;
 * expFactor     = '^' factor;
 * phrase        = '(' expression ')' | Num;
 * ```
 *
 * These rules recognize conventional operator precedence and  associativity. They also avoid the problem of left
 * recursion, and their implementation avoids problems with the infinite loop inherent in the cyclic dependencies of
 * the rules. In other words, the rules may look simple, but their structure is subtle.
 *
 * @author Steven J. Metsker, Alan K. Sun
 *
 * @version 2.0
 */

const val ERR_IMPROPERLY_FORMED = "Improperly formed arithmetic expression"
const val ERR_INTERNAL_ERROR = "Internal error in ArithmeticParser"

fun calculate(exp: String): Double {
    val parser = KArithmeticParser().expression()
    val value = parser.completeMatch(KTokenAssembly(exp)) ?: throw Exception(ERR_IMPROPERLY_FORMED)
    return value.pop() as? Double ?: throw RuntimeException(ERR_INTERNAL_ERROR)
}

class KArithmeticParser {

    private lateinit var expr: KSequence<KToken>
    private lateinit var fact: KAlternation<KToken>

    /**
     * Returns a parser that will recognize an arithmetic expression.
     *
     * ```
     *  expression = term (plusTerm | minusTerm)*
     * ```
     *
     * @return a parser that will recognize an arithmetic expression
     */
    fun expression(level: Int = 0): KParser<KToken> {
        if (!this::expr.isInitialized) {
            expr = KSequence("Expression", level)
            expr.add(term(level+1))
            val alt = KAlternation<KToken>(level = level+2)
            alt.add(plusTerm(level+2))
            alt.add(minusTerm(level+2))
            expr.add(KRepetition(level = level+1, subParser = alt))
        }
        return expr
    }

    /**
     * Returns a parser that for the grammar rule:
     *
     * ```
     *     divideFactor = '/' factor;
     * ```
     *
     * This parser has an assembler that will pop two numbers from the stack and push their quotient.
     */
    private fun divideFactor(level: Int = 0): KParser<KToken> {
        val div = KSequence<KToken>("DivideFactor", level)
        div.add(KSymbol('/', level+1).discard())
        div.add(factor(level+1))
        div.setAssembler(KDivideAssembler())
        return div
    }

    /**
     * Returns a parser that for the grammar rule:
     *
     *  ```
     *     expFactor = '^' factor;
     *  ```
     *
     * This parser has an assembler that will pop two numbers from the stack and push the result of
     * exponentiation the lower number to the upper one.
     */
    private fun expFactor(level: Int = 0): KParser<KToken> {
        val exp = KSequence<KToken>("ExpFactor", level)
        exp.add(KSymbol('^', level+1).discard())
        exp.add(factor(level+1))
        exp.setAssembler(KExpAssembler())
        return exp
    }


    /**
     * Returns a parser that for the grammar rule:
     *
     * ```
     *     factor = phrase expFactor | phrase;
     * ```
     */
    private fun factor(level: Int = 0): KParser<KToken> {
        /*
         * This use of a static variable avoids the infinite recursion inherent in the grammar; factor depends
         * on expFactor, and expFactor depends on factor.
         */
        if(!this::fact.isInitialized) {
            fact = KAlternation("Factor", level)
            val seq = KSequence<KToken>(level = level+1)
            seq.add(phrase(level+2))
            seq.add(expFactor(level+2))
            fact.add(seq)
            fact.add(phrase(level+1))
        }
        return fact
    }

    /**
     * Returns a parser that for the grammar rule:
     *
     * ```
     *     minusTerm = '-' term;
     * ```
     *
     * This parser has an assembler that will pop two numbers from the stack and push their difference.
     */
    private fun minusTerm(level: Int = 0): KParser<KToken> {
        val minus = KSequence<KToken>("MinusTerm", level)
        minus.add(KSymbol('-', level+1).discard())
        minus.add(term(level+1))
        minus.setAssembler(KMinusAssembler())
        return minus
    }

    /**
     * Returns a parser that for the grammar rule:
     * ```
     *
     *    phrase = '(' expression ')' | Num;
     *
     * ```
     * This parser adds an assembler to Num, that will
     * replace the top token in the stack with the KToken's
     * Double value.
     */
    private fun phrase(level: Int = 0): KParser<KToken> {
        val phrase = KAlternation<KToken>("Phrase", level)
        val seq = KSequence<KToken>(level = level+1)
        seq.add(KSymbol('(', level+2).discard())
        seq.add(expression(level+2))
        seq.add(KSymbol(')', level+2).discard())
        phrase.add(seq)
        phrase.add(KNum(level = level+1).setAssembler(KNumAssembler()))
        return phrase
    }

    /**
     * Returns a parser that for the grammar rule:
     * ```
     *
     *     plusTerm = '+' term;
     *
     * ```
     * This parser has an assembler that will pop two numbers from the stack and push their sum.
     */
    private fun plusTerm(level: Int = 0): KParser<KToken> {
        val plus = KSequence<KToken>("PlusTerm", level)
        plus.add(KSymbol('+', level+1).discard())
        plus.add(term(level+1))
        plus.setAssembler(KPlusAssembler())
        return plus
    }

    /**
     * Returns a parser that for the grammar rule:
     * ```
     *
     *    term = factor (timesFactor | divideFactor)*;
     * ```
     */
    private fun term(level: Int = 0): KParser<KToken> {
        val term = KSequence<KToken>("Term", level)
        term.add(factor(level+1))
        val alt = KAlternation<KToken>(level = level+1)
        alt.add(timesFactor(level+2))
        alt.add(divideFactor(level+2))
        term.add(KRepetition(level = level+1, subParser = alt))
        return term
    }

    /**
     * Returns a parser that for the grammar rule:
     * ```
     *
     *     timesFactor = '*' factor;
     *
     * ```
     * This parser has an assembler that will pop two numbers from the stack and push their product.
     */
    private fun timesFactor(level: Int = 0): KParser<KToken> {
        val time = KSequence<KToken>("TimesFactor", level)
        time.add(KSymbol('*', level+1).discard())
        time.add(factor(level+1))
        time.setAssembler(KTimesAssembler())
        return time
    }
}