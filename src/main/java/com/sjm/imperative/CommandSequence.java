package com.sjm.imperative;

import java.util.*;
import java.util.stream.Collectors;
/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 *
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose,
 * including the implied warranty of merchantability.
 */

/**
 * This class contains a sequence of other commands.
 *
 * @author Steven J. Metsker
 * @version 1.0
 */
public class CommandSequence extends Command {
    protected List<Command> commands;

    /**
     * Add a command to the sequence of commands to which this
     * object will cascade an <code>execute</code> command.
     *
     * @param c a command to add to this command sequence
     */
    public void addCommand(Command c) {
        commands().add(c);
    }

    /**
     * Lazy-initialize the <code>commands</code> List.
     */
    protected List<Command> commands() {
        if (commands == null) {
            commands = new ArrayList();
        }
        return commands;
    }

    /**
     * Ask each command in the sequence to <code>execute</code>.
     */
    public void execute() {
    	for (Command cmd: commands()) {
			Thread.yield();
			cmd.execute();
		}
    }

    /**
     * Returns a string description of this command sequence.
     *
     * @return a string description of this command sequence
     */
    public String toString() {
    	return String.join("\n", commands().stream().map(cmd -> cmd.toString()).collect(Collectors.toList()));
    }
}
