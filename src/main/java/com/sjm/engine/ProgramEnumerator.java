package com.sjm.engine;

import java.util.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * A ProgramEnumerator returns the axioms of a program,
 * one at a time.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public class ProgramEnumerator implements AxiomEnumeration {
    protected Iterator<Axiom> itr;

    /**
     * Construct an enumeration of the given program.
     *
     * @param p the program to enumerate over
     */
    public ProgramEnumerator(Program p) {
        itr = p.axioms.iterator();
    }

    /**
     * Tests if this enumeration contains more axioms.
     *
     * @return <code>true</code> if the program this enumeration
     * is constructed for contains more axioms, and
     * <code>false</code> otherwise.
     */
    public boolean hasMoreAxioms() {
        return itr.hasNext();
    }

    /**
     * Returns the next axiom of this enumeration.
     *
     * @return the next axiom of this enumeration.
     */
    public Axiom nextAxiom() {
        return itr.next();
    }
}
