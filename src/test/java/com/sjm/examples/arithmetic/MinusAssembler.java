package com.sjm.examples.arithmetic;

import com.sjm.parse.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * Pop two numbers from the stack and push the result of
 * subtracting the top number from the one below it.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public class MinusAssembler extends Assembler {
    /**
     * Pop two numbers from the stack and push the result of
     * subtracting the top number from the one below it.
     *
     * @param a the assembly whose stack to use
     */
    public void workOn(Assembly a) {
        Double d1 = (Double) a.pop();
        Double d2 = (Double) a.pop();
        a.push(d2 - d1);
    }
}
