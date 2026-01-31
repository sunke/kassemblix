package com.sjm.parse;

import java.util.*;
import java.util.stream.Collectors;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * This class abstracts the behavior common to parsers
 * that consist of a series of other parsers.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public abstract class CollectionParser extends Parser {
    /**
     * the parsers this parser is a collection of
     */
    protected List<Parser> subparsers = new ArrayList();

    /**
     * Supports subclass constructors with no arguments.
     */
    public CollectionParser() {
    }

    /**
     * Supports subclass constructors with a name argument
     *
     * @param name the name of this parser
     */
    public CollectionParser(String name) {
        super(name);
    }

    /**
     * Adds a parser to the collection.
     *
     * @param e the parser to add
     * @return this
     */
    public CollectionParser add(Parser e) {
        subparsers.add(e);
        return this;
    }

    /**
     * Return this parser's subparsers.
     *
     * @return List   this parser's subparsers
     */
    public List getSubparsers() {
        return subparsers;
    }

    /**
     * Helps to textually describe this CollectionParser.
     *
     * @returns the string to place between parsers in
     * the collection
     */
    protected abstract String toStringSeparator();

    /*
     * Returns a textual description of this parser.
     */
    protected String unvisitedString(List visited) {
        StringBuffer buf = new StringBuffer("<");
        buf.append(String.join(toStringSeparator(),
                subparsers.stream().map(p -> p.toString(visited)).collect(Collectors.toList())));
        buf.append(">");
        return buf.toString();
    }
}
