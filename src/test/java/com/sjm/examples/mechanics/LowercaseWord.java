package com.sjm.examples.mechanics;

import java.util.*;

import com.sjm.parse.tokens.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class shows the how to introduce a new type of 
 * terminal, specifically for recognizing lowercase words.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class LowercaseWord extends Word {

/**
 * Returns true if an assembly's next element is a 
 * lower case word.
 *
 * @param   object   an element from a assembly
 *
 * @return   true, if an assembly's next element is a 
 *           lowercase word
 */
protected boolean qualifies (Object o) {
	Token t = (Token) o;
	if (!t.isWord()) {
		return false;
	}	
	String word = t.sval();
	return word.length() > 0 && 
	       Character.isLowerCase(word.charAt(0));
}
/**
 * 
 */
public List randomExpansion(int maxDepth, int depth) {
	int n = (int) (5.0 * Math.random()) + 3;
	
	char[] letters = new char[n];
	for (int i = 0; i < n; i++) {
		int c = (int) (26.0 * Math.random()) + 'a';
		letters[i] = (char) c;
	}
	
	List v = new ArrayList();
	v.add(new String(letters));
	return v;
}
/**
 * Returns a textual description of this production.
 *
 * @param   List   a list of productions already printed in this
 *                   description
 * 
 * @return   string   a textual description of this production
 *
 * @see ProductionRule#toString()
 */
public String unvisitedString(java.util.List visited) {
	return "word";
}
}
