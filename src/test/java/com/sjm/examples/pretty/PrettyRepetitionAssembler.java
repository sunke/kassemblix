package com.sjm.examples.pretty;

import java.util.*;

import com.sjm.parse.*;
/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * Replace the nodes above a given "fence" object with
 * a new composite that holds the popped nodes as its children.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public class PrettyRepetitionAssembler extends Assembler {
    protected String name;
    protected Object fence;

    /**
     * Construct an assembler that will replace the nodes above the
     * supplied "fence" object with a new composite that will hold
     * the popped nodes as its children.
     */
    public PrettyRepetitionAssembler(String name, Object fence) {
        this.name = name;
        this.fence = fence;
    }

    /**
     * Replace the nodes above a given "fence" object with
     * a new composite that holds the popped nodes as its children.
     *
     * @param a the assembly to work on
     */
    public void workOn(Assembly a) {
        CompositeNode newNode = new CompositeNode(name);
        List<CompositeNode> v = elementsAbove(a, fence);
        for (CompositeNode e : v) {
            newNode.add(e);
        }
        a.push(newNode);
    }
}
