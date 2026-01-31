package com.sjm.engine;

import java.util.*;
/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * A Unification is a collection of variables.
 * <p>
 * Structures and variables use unifications to keep track of the
 * variable assignments that make a proof work. The unification
 * class itself provides behavior for adding and accessing
 * variables.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */

public class Unification {
    public static final Unification empty = new Unification();
    List List;

    /**
     * Creates an empty unification.
     */
    public Unification() {
    }

    /**
     * Creates a unification that starts off including a single
     * variable.
     *
     * @param v the variable with which the unification
     *                 begins
     */
    public Unification(Variable v) {
        addVariable(v);
    }

    /**
     * Adds a variable to this unification.
     *
     * @param v the variable to add to this unification
     * @return this unification
     */
    public Unification addVariable(Variable v) {
        if (!List().contains(v)) {
            List.add(v);
        }
        return this;
    }

    /**
     * Adds all the variables of another unification to this one.
     *
     * @param u the unification to append
     * @return this unification
     */
    public Unification append(Unification u) {
        for (int i = 0; i < u.size(); i++) {
            addVariable(u.variableAt(i));
        }
        return this;
    }

    /**
     * Return the variables in this unification.
     *
     * @return the variables in this unification.
     */
    public Iterator<Variable> elements() {
        return List().iterator();
    }

    /**
     * Returns the number of variables in this unification.
     *
     * @return int   the number of variables in this unification
     */
    public int size() {
        if (List == null) {
            return 0;
        }
        return List.size();
    }

    /**
     * Returns a string representation of this unification.
     *
     * @return a string representation of this unification
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(variableAt(i).definitionString());
        }
        return buf.toString();
    }

    /**
     * Returns a string representation of this unification,
     * without printing variable names.
     *
     * @return a string representation of this unification,
     * without printing variable names
     */
    public String toStringQuiet() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(variableAt(i));
        }
        return buf.toString();
    }

    /**
     * Asks all the contained variables to unbind.
     */
    public void unbind() {
        for (int i = 0; i < size(); i++) {
            variableAt(i).unbind();
        }
    }

    /*
     * Returns the variable at the indicated index.
     *
     * @param   int   the index of the variable to return
     *
     * @return   variable   the variable at the indicated index
     */
    protected Variable variableAt(int i) {
        return (Variable) List().get(i);
    }

    /*
     * lazy-initialize this unification's List
     */
    protected List List() {
        if (List == null) {
            List = new ArrayList();
        }
        return List;
    }
}
