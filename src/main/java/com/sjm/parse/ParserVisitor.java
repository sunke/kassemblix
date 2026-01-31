package com.sjm.parse;

import java.util.*;
/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This class provides a "visitor" hierarchy in support of
 * the Visitor pattern -- see the book, "Design Patterns" for
 * an explanation of this pattern.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public abstract class ParserVisitor {
/**
 * Visit an alternation.
 *
 * @param   a   the parser to visit
 *
 * @param   visited   a collection of previously visited parsers
 *
 */
public abstract void visitAlternation(
	Alternation a, List visited);
/**
 * Visit an empty parser.
 *
 * @param   e   the parser to visit
 *
 * @param   visited   a collection of previously visited parsers
 *
 */
public abstract void visitEmpty(Empty e, List visited);
/**
 * Visit a repetition.
 *
 * @param   r   the parser to visit
 *
 * @param   visited   a collection of previously visited parsers
 *
 */
public abstract void visitRepetition(
	Repetition r, List visited);
/**
 * Visit a sequence.
 *
 * @param   s   the parser to visit
 *
 * @param   visited   a collection of previously visited parsers
 *
 */
public abstract void visitSequence(Sequence s, List visited);
/**
 * Visit a terminal.
 *
 * @param   t   the parser to visit
 *
 * @param   visited   a collection of previously visited parsers
 *
 */
public abstract void visitTerminal(Terminal t, List visited);
}
