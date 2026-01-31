package net.codenest.kassemblix.parser

import net.codenest.kassemblix.lexer.KToken
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows

class KAmbiguityTest {

    @Test
    fun testAmbiguity() {
        val parser = getParser()

        // "cups" qualify as both a Word value and a volume literal
        val exception = assertThrows(Exception::class.java)
            { parser.completeMatch(KTokenAssembly("How many cups are in a gallon?")) }

        assertEquals(ERR_AMBIGUOUS_GRAMMAR, exception.message)
    }

    /**
     * Grammar:
     *      main = (Word | volume)* '?'
     *      volume = 'cup' | 'gallon' | 'liter'
     */
    private fun getParser(): KParser<KToken> {
        val vol = KAlternation<KToken>(level = 3)
                .add(KLiteral("cups", level = 4))
                .add(KLiteral("gallon", level = 4))
                .add(KLiteral("liter", level = 4))

        val alt = KAlternation<KToken>(level = 2)
                .add(KWord(level = 3))
                .add(vol)

        return KSequence<KToken>(level = 0)
                .add(KRepetition<KToken>(subParser = alt, level = 1))
                .add(KSymbol('?', level = 1))
    }
}