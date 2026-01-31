package com.sjm.examples.track;

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
 * A Track is a sequence that throws a <code>
 * TrackException</code> if the sequence begins but
 * does not complete.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */

public class Track extends Sequence {
/**
 * Constructs a nameless Track.
 */
public Track () {
}
/**
 * Constructs a Track with the given name.
 *
 * @param    name    a name to be known by
 */
public Track(String name) {
	super(name);
}
/**
 * Given a collection of assemblies, this method matches 
 * this track against all of them, and returns a new 
 * collection of the assemblies that result from the 
 * matches.
 *
 * If the match begins but does not complete, this method
 * throws a <code>TrackException</code>.
 *
 * @return   a List of assemblies that result from matching
 *           against a beginning set of assemblies
 *
 * @param in a List of assemblies to match against
 *
 */
public List match(List in) {
	boolean inTrack = false;
	List last = in;
	List out = in;
	for (Parser p: subparsers) {
		out = p.matchAndAssemble(last);
		if (out.isEmpty()) {
			if (inTrack) {
				throwTrackException(last, p);
			}
			return out;
		}
		inTrack = true;
		last = out;
	}
	return out;
}
/*
 * Throw an exception showing how far the match had 
 * progressed, what it found next, and what it was 
 * expecting.
 */
protected void throwTrackException(
	List previousState, Parser p) {
		
	Assembly best = best(previousState);
	String after = best.consumed(" ");
	if (after.equals("")) {
		after = "-nothing-";
	}
	
	String expected = p.toString();
	
	Object next = best.peek();
	String found = 
		(next == null) ? "-nothing-" : next.toString();
		
	throw new TrackException(after, expected, found);
}
}
