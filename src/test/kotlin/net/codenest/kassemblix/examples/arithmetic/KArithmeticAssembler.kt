package net.codenest.kassemblix.example.arithmetic

import net.codenest.kassemblix.lexer.KToken
import net.codenest.kassemblix.parser.KAssembly
import net.codenest.kassemblix.parser.KTokenAssembler
import kotlin.math.pow

class KDivideAssembler : KTokenAssembler() {
    /**
     * Pop two numbers from the stack and push the result of dividing the top number into the one below it.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2: Double = assembly.pop() as Double
        assembly.push(d2 / d1)
    }
}

class KExpAssembler : KTokenAssembler() {
    /**
     * Pop two numbers from the stack and push the result of exponentiation the lower number to the upper one.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2.pow(d1))
    }
}

class KMinusAssembler : KTokenAssembler()  {
    /**
     * Pop two numbers from the stack and push the result of subtracting the top number from the one below it.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 - d1)
    }
}

class KNumAssembler : KTokenAssembler() {
    /**
     * Replace the top token in the stack with the token's Double value.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val t = assembly.pop() as KToken
        assembly.push(t.nval)
    }
}

class KPlusAssembler : KTokenAssembler() {
    /**
     * Pop two numbers from the stack and push their sum.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 + d1)
    }
}

class KTimesAssembler : KTokenAssembler() {
    /**
     * Pop two numbers from the stack and push the result of multiplying the top number by the one below it.
     */
    override fun workOn(assembly: KAssembly<KToken>) {
        val d1 = assembly.pop() as Double
        val d2 = assembly.pop() as Double
        assembly.push(d2 * d1)
    }
}