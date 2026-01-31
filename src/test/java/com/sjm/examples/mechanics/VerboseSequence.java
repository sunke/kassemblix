package com.sjm.examples.mechanics;

import java.util.*;
import com.sjm.parse.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * The <code>match()</code> method of this class prints the 
 * collection of assemblies it receives, and each new 
 * collection it creates.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class VerboseSequence extends Sequence {
/**
 * Constructs a nameless VerboseSequence.
 */
public VerboseSequence() {
	super();
}
/**
 * Constructs a Sequence with the given name.
 *
 * @param    name    a name to be known by
 */
public VerboseSequence(String name) {
	super(name);
}
/**
 * Just a verbose version of <code>Sequence.match()</code>.
 */
public List match(List inputState) {
	List finalState = inputState;
	System.out.println(finalState); // be verbose
	for (Parser p: subparsers) {
		finalState = p.matchAndAssemble(finalState);
		if (finalState.isEmpty()) {
			return finalState;
		}
		System.out.println(finalState); // be verbose
	}
	return finalState;
}
}
