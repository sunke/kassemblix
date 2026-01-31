package com.sjm.examples.arithmetic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArithmeticParserTest {

    @Test
    public void testArithmeticParser() {
        assertResult("9^2 - 81       ", 0); // exponentiation
        assertResult("7 - 3 - 1      ", 3); // minus associativity
        assertResult("2 ^ 1 ^ 4      ", 2); // exp associativity
        assertResult("100 - 25*3     ", 25); // precedence
        assertResult("100 - 5^2*3    ", 25); // precedence
        assertResult("(100 - 5^2) * 3", 225); // parentheses
    }

    private static void assertResult(String s, double d) {
        assertEquals(d, ArithmeticParser.value(s));
    }
}