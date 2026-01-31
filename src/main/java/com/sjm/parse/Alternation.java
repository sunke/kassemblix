package com.sjm.parse;

import java.util.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * An <code>Alternation</code> object is a collection of
 * parsers, any one of which can successfully match against
 * an assembly.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */

public class Alternation extends CollectionParser {

    /**
     * Constructs a nameless alternation.
     */
    public Alternation() {
    }

    /**
     * Constructs an alternation with the given name.
     *
     * @param name a name to be known by
     */
    public Alternation(String name) {
        super(name);
    }

    /**
     * Accept a "visitor" and a collection of previously visited
     * parsers.
     *
     * @param pv      the visitor to accept
     * @param visited a collection of previously visited parsers
     */
    public void accept(ParserVisitor pv, List visited) {
        pv.visitAlternation(this, visited);
    }

    /**
     * Given a set of assemblies, this method matches this
     * alternation against all of them, and returns a new set
     * of the assemblies that result from the matches.
     *
     * @param in a List of assemblies to match against
     * @return a List of assemblies that result from
     * matching against a beginning set of assemblies
     */
    public List match(List in) {
        List out = new ArrayList();
        for (Parser p : subparsers) {
            add(out, p.matchAndAssemble(in));
        }
        return out;
    }

    /*
     * Create a random collection of elements that correspond to
     * this alternation.
     */
    protected List randomExpansion(int maxDepth, int depth) {
        if (depth >= maxDepth) {
            return randomSettle(maxDepth, depth);
        }
        double n = subparsers.size();
        int i = (int) (n * Math.random());
        Parser j = subparsers.get(i);
        return j.randomExpansion(maxDepth, depth++);
    }

    /*
     * This method is similar to randomExpansion, but it will
     * pick a terminal if one is available.
     */
    protected List randomSettle(int maxDepth, int depth) {

        // which alternatives are terminals?

        List<Parser> terms = new ArrayList();
        for (Parser p : subparsers) {
            if (p instanceof Terminal) {
                terms.add(p);
            }
        }

        // pick one of the terminals or, if there are no
        // terminals, pick any subparser

        List which = terms;
        if (terms.isEmpty()) {
            which = subparsers;
        }

        double n = which.size();
        int i = (int) (n * Math.random());
        Parser p = (Parser) which.get(i);
        return p.randomExpansion(maxDepth, depth++);
    }

    /*
     * Returns the string to show between the parsers this
     * parser is an alternation of.
     */
    protected String toStringSeparator() {
        return "|";
    }
}
