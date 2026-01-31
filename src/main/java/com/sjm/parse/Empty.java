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
 * An <code>Empty</code> parser matches any assembly once, 
 * and applies its assembler that one time.
 * <p>
 * Language elements often contain empty parts. For example, 
 * a language may at some point allow a list of parameters
 * in parentheses, and may allow an empty list. An empty
 * parser makes it easy to match, within the 
 * parenthesis, either a list of parameters or "empty".
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 * 
 */
public class Empty extends Parser {
/**
 * Accept a "visitor" and a collection of previously visited
 * parsers.
 *
 * @param   pv   the visitor to accept
 *
 * @param   visited   a collection of previously visited parsers
 */
public void accept(ParserVisitor pv, List visited) {
	pv.visitEmpty(this, visited);
}
/**
 * Given a set of assemblies, this method returns the set as
 * a successful match.
 * 
 * @return   the input set of states
 *
 * @param   in   a List of assemblies to match against
 *
 */
public List match(List in) {
	return elementClone(in);
}
/*
 * There really is no way to expand an empty parser, so
 * return an empty List.
 */
protected List randomExpansion(int maxDepth, int depth) {
	return new ArrayList();
}
/*
 * Returns a textual description of this parser.
 */
 protected String unvisitedString(List visited) {
	return " empty ";
}
}
