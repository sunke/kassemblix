package com.sjm.examples.query;

import java.util.*;
import com.sjm.engine.*;
import com.sjm.utensil.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class accepts terms, class names and comparisons,
 * and then builds a query from them.
 *
 * The query this builder creates will have the form:
 *
 * <blockquote><pre>	
 *     q(term0, term1, ...), 
 *     className0, className1, ...,
 *     comparison0, comparison1, ...
 * </pre></blockquote>
 *
 * The first structure forms a "projection", which is
 * set of terms that should all be valid after the
 * remaining structures prove themselves. To use the query
 * after building it, prove its tail to establish values
 * for its head.
 *
 * For example, consider the select statement:
 *
 * <blockquote><pre>
 *    select PricePerBag * 0.9 
 *        from chip 
 *        where PricePerBag > 10
 * </pre></blockquote>
 *
 * a parser for this statement can pass a QueryBuilder
 * one term, one class name and one comparison; the builder
 * will take these and build the query:
 *
 * <blockquote><pre>
 *     q(*(PricePerBag, 0.9)), 
 *     chip(ChipID, ChipName, PricePerBag, Ounces, Oil), 
 *     >(PricePerBag, 10.0)
 * </pre></blockquote>
 *
 * A program can prove the tail of this query (that is, all
 * the structures after the first). Each proof of the tail
 * will establish a value for the PricePerBag variable.
 * After each proof, the term in the head structure will
 * have a value of .9 times the PricePerBag.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class QueryBuilder implements PubliclyCloneable {
	protected Speller speller;
	protected List<Term> terms = new ArrayList();
	protected List<String> classNames = new ArrayList();
	protected List<Comparison> comparisons = new ArrayList();
/**
 * Construct a query builder that will use the given speller.
 */
public QueryBuilder(Speller speller) {
	this.speller = speller;
}
/**
 * Add the given class name to the query. This method
 * checks that the class name when properly spelled matches
 * a known class name.
 */
public void addClassName(String s) {
	String properName = speller.getClassName(s);
	if (properName == null) {
		throw new UnrecognizedClassException(
			"No class named " + s + " in object model");
	}
	classNames.add(properName);
}
/**
 * Add a comparison to the query.
 */
public void addComparison(Comparison c) {
	comparisons.add(c);
}
/**
 * Add a term that will appear in the head structure of
 * the query.
 */
public void addTerm(Term t) { 
	terms.add(t);
}
/**
 * Create a query from the terms, class names and variables
 * this object has received so far.
 */
public Query build(AxiomSource as) {
	List<Structure> structures = new ArrayList();

	// create the "projection" structure
	Term[] termArray = new Term[terms.size()];
	termArray = terms.toArray(termArray);
	Structure s = new Structure("q", termArray);
	structures.add(s);

	// add each queried table
	classNames.forEach(name -> structures.add(ChipSource.queryStructure(name)));

	// add each comparison
	comparisons.forEach(cmp -> structures.add(cmp));

	// create and return a query
	Structure[] sarray = new Structure[structures.size()];
	sarray = structures.toArray(sarray);
	return new Query(as, sarray);
}
/**
 * Return a copy of this object.
 *
 * @return a copy of this object
 */
public Object clone() {
	try {
		QueryBuilder c = (QueryBuilder) super.clone();
		//c.terms = (List) terms.clone();
		//c.classNames = (List) classNames.clone();
		//c.comparisons = (List) comparisons.clone();
		c.terms.addAll(terms);
		c.classNames.addAll(classNames);
		c.comparisons.addAll(comparisons);
		return c;
	} catch (CloneNotSupportedException e) {
		// this shouldn't happen, since we are Cloneable
		throw new InternalError();
	}
}
}
