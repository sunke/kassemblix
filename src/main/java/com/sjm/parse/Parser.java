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
 * A <code>Parser</code> is an object that recognizes the
 * elements of a language.
 * <p>
 * Each <code>Parser</code> object is either a <code>
 * Terminal</code> or a composition of other parsers.
 * The <code>Terminal</code> class is a subclass of <code>
 * Parser</code>, and is itself a hierarchy of
 * parsers that recognize specific patterns of text. For
 * example, a <code>Word</code> recognizes any word, and a
 * <code>Literal</code> matches a specific string.
 * <p>
 * In addition to <code>Terminal</code>, other subclasses of
 * <code>Parser</code> provide composite parsers,
 * describing sequences, alternations, and repetitions of
 * other parsers. For example, the following <code>
 * Parser</code> objects culminate in a <code>good
 * </code> parser that recognizes a description of good
 * coffee.
 *
 * <blockquote><pre>
 *     Alternation adjective = new Alternation();
 *     adjective.add(new Literal("steaming"));
 *     adjective.add(new Literal("hot"));
 *     Sequence good = new Sequence();
 *     good.add(new Repetition(adjective));
 *     good.add(new Literal("coffee"));
 *     String s = "hot hot steaming hot coffee";
 *     Assembly a = new TokenAssembly(s);
 *     System.out.println(good.bestMatch(a));
 * </pre></blockquote>
 * <p>
 * This prints out:
 *
 * <blockquote><pre>
 *     [hot, hot, steaming, hot, coffee]
 *     hot/hot/steaming/hot/coffee^
 * </pre></blockquote>
 * <p>
 * The parser does not match directly against a string,
 * it matches against an <code>Assembly</code>.  The
 * resulting assembly shows its stack, with four words on it,
 * along with its sequence of tokens, and the index at the
 * end of these. In practice, parsers will do some work
 * on an assembly, based on the text they recognize.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */

public abstract class Parser {

    /**
     * a name to identify this parser
     */
    protected String name;

    /**
     * an object that will work on an assembly whenever this parser successfully matches against the assembly
     */
    protected Assembler assembler;

    public Parser() {
    }

    public Parser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Accepts a "visitor" which will perform some operation on a parser structure.
     *
     * @param pv the visitor to accept
     */
    public void accept(ParserVisitor pv) {
        accept(pv, new ArrayList());
    }

    /**
     * Accepts a "visitor" along with a collection of previously visited parsers.
     *
     * @param pv      the visitor to accept
     * @param visited a collection of previously visited
     *                parsers.
     */
    public abstract void accept(ParserVisitor pv, List visited);

    /**
     * Adds the elements of one List to another.
     *
     * @param v1 the List to add to
     * @param v2 the List with elements to add
     */
    public static void add(List v1, List v2) {
        v1.addAll(v2);
    }

    /**
     * Returns the most-matched assembly in a collection.
     *
     * @param v the collection to look through
     * @return the most-matched assembly in a collection.
     */
    public Assembly best(List v) {
        Assembly best = null;
        for (Assembly a : (List<Assembly>) v) {
            if (!a.hasMoreElements()) {
                return a;
            }
            if (best == null) {
                best = a;
            } else if (a.elementsConsumed() >
                    best.elementsConsumed()) {

                best = a;
            }
        }
        return best;
    }

    /**
     * Returns an assembly with the greatest possible number of
     * elements consumed by matches of this parser.
     *
     * @param a an assembly to match against
     * @return an assembly with the greatest possible number of
     * elements consumed by this parser
     */
    public Assembly bestMatch(Assembly a) {
        List in = new ArrayList();
        in.add(a);
        List out = matchAndAssemble(in);
        return best(out);
    }

    /**
     * Returns either null, or a completely matched version of
     * the supplied assembly.
     *
     * @param a an assembly to match against
     * @return either null, or a completely matched version of the
     * supplied assembly
     */
    public Assembly completeMatch(Assembly a) {
        Assembly best = bestMatch(a);
        if (best != null && !best.hasMoreElements()) {
            return best;
        }
        return null;
    }

    /**
     * Create a copy of a List, cloning each element of
     * the List.
     *
     * @param v the List to copy
     * @return a copy of the input List, cloning each
     * element of the List
     */
    public static List elementClone(List v) {
        List copy = new ArrayList();
        for(Assembly a: (List<Assembly>) v) {
            copy.add(a.clone());
        }
        return copy;
    }

    /**
     * Given a set (well, a <code>List</code>, really) of
     * assemblies, this method matches this parser against
     * all of them, and returns a new set (also really a
     * <code>List</code>) of the assemblies that result from
     * the matches.
     * <p>
     * For example, consider matching the regular expression
     * <code>a*</code> against the string <code>"aaab"</code>.
     * The initial set of states is <code>{^aaab}</code>, where
     * the ^ indicates how far along the assembly is. When
     * <code>a*</code> matches against this initial state, it
     * creates a new set <code>{^aaab, a^aab, aa^ab,
     * aaa^b}</code>.
     *
     * @param in a List of assemblies to match against
     * @return a List of assemblies that result from
     * matching against a beginning set of assemblies
     */
    public abstract List match(List in);

    /**
     * Match this parser against an input state, and then
     * apply this parser's assembler against the resulting
     * state.
     *
     * @param in a List of assemblies to match against
     * @return a List of assemblies that result from matching
     * against a beginning set of assemblies
     */
    public List matchAndAssemble(List in) {
        List out = match(in);
        if (assembler != null) {
            for (Assembly a: (List<Assembly>) out) {
                assembler.workOn(a);
            }
        }
        return out;
    }

    /*
     * Create a random expansion for this parser, where a
     * concatenation of the returned collection will be a
     * language element.
     */
    protected abstract List randomExpansion(int maxDepth, int depth);

    /**
     * Return a random element of this parser's language.
     *
     * @return a random element of this parser's language
     */
    public String randomInput(int maxDepth, String separator) {
        return String.join(separator, (List<String>) randomExpansion(maxDepth, 0));
    }

    /**
     * Sets the object that will work on an assembly whenever
     * this parser successfully matches against the
     * assembly.
     *
     * @param assembler the assembler to apply
     * @return Parser   this
     */
    public Parser setAssembler(Assembler assembler) {
        this.assembler = assembler;
        return this;
    }

    /**
     * Returns a textual description of this parser.
     *
     * @return String   a textual description of this
     * parser, taking care to avoid
     * infinite recursion
     */
    public String toString() {
        return toString(new ArrayList());
    }

    /**
     * Returns a textual description of this parser.
     * Parsers can be recursive, so when building a
     * descriptive string, it is important to avoid infinite
     * recursion by keeping track of the objects already
     * described. This method keeps an object from printing
     * twice, and uses <code>unvisitedString</code> which
     * subclasses must implement.
     *
     * @param visited a list of objects already printed
     * @return a textual version of this parser,
     * avoiding recursion
     */
    protected String toString(List visited) {
        if (name != null) {
            return name;
        } else if (visited.contains(this)) {
            return "...";
        } else {
            visited.add(this);
            return unvisitedString(visited);
        }
    }

    /*
     * Returns a textual description of this string.
     */
    protected abstract String unvisitedString(List visited);
}
