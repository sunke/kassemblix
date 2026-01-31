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
 * A <code>Repetition</code> matches its underlying parser 
 * repeatedly against a assembly.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */

public class Repetition extends Parser {
	/*
	 * the parser this parser is a repetition of
	 */
	protected Parser subparser;

	/*
	 * the width of a random expansion
	 */
	protected static final int EXPWIDTH = 4;
	
	/*
	 * an assembler to apply at the beginning of a match
	 */
	protected Assembler preAssembler;
/**
 * Constructs a repetition of the given parser. 
 * 
 * @param   p  the parser to repeat
 *
 * @return   a repetiton that will match the given 
 *           parser repeatedly in successive matches
 */
public Repetition (Parser p) {
	this(p, null);
}
/**
 * Constructs a repetition of the given parser with the
 * given name.
 * 
 * @param   subparser   the parser to repeat
 *
 * @param   name name to be known by
 *
 * @return   a repetiton that will match the given 
 *           parser repeatedly in successive matches
 */
public Repetition(Parser subparser, String name) {
	super(name);
	this.subparser = subparser;
}
/**
 * Accept a "visitor" and a collection of previously visited
 * parsers.
 *
 * @param   pv   the visitor to accept
 *
 * @param   visited   a collection of previously visited parsers
 */
public void accept(ParserVisitor pv, List visited) {
	pv.visitRepetition(this, visited);
}
/**
 * Return this parser's subparser.
 *
 * @return   Parser   this parser's subparser
 */
public Parser getSubparser() {
	return subparser;
}
/**
 * Given a set of assemblies, this method applies a preassembler
 * to all of them, matches its subparser repeatedly against each
 * of them, applies its post-assembler against each, and returns
 * a new set of the assemblies that result from the matches.
 * <p>
 * For example, matching the regular expression <code>a*
 * </code> against <code>{^aaab}</code> results in <code>
 * {^aaab, a^aab, aa^ab, aaa^b}</code>.
 *
 * @return   a List of assemblies that result from
 *           matching against a beginning set of assemblies
 *
 * @param   in   a List of assemblies to match against
 *
 */
public List match(List in) {
	if (preAssembler != null) {
		in.stream().forEach(a -> preAssembler.workOn((Assembly) a));
	}
	List out = elementClone(in);
	List s = in; // a working state
	while (!s.isEmpty()) {
		s = subparser.matchAndAssemble(s);
		add(out, s);
	}
	return out;
}
/**
 * Create a collection of random elements that correspond to
 * this repetition.
 */
protected List randomExpansion(int maxDepth, int depth) {
	List v = new ArrayList();
	if (depth >= maxDepth) {
		return v;
	}

	int n = (int) (EXPWIDTH * Math.random());
	for (int j = 0; j < n; j++) {
		v.addAll(subparser.randomExpansion(maxDepth, depth++));
	}
	return v;
}
/**
 * Sets the object that will work on every assembly before 
 * matching against it.
 *
 * @param   preAssembler   the assembler to apply
 *
 * @return   Parser   this
 */
public Parser setPreAssembler(Assembler preAssembler) {
	this.preAssembler = preAssembler;
	return this;
}
/*
 * Returns a textual description of this parser.
 */
 protected String unvisitedString(List visited) {
	return subparser.toString(visited) + "*";
}
}
