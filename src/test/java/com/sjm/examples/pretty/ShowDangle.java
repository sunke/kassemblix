package com.sjm.examples.pretty;

import java.util.*;

import com.sjm.parse.tokens.*;
import com.sjm.examples.tests.*;
/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * Show that the <code>Dangle.statement()</code> parser
 * is ambiguous.
 * 
 * @author Steven J. Metsker
 * 
 * @version 1.0 
 */
public class ShowDangle {
	/**
	 * Show that the <code>Dangle.statement()</code> parser
	 * is ambiguous.
	 */
	public static void main(String[] args) {
		String s;
		s = "if (overdueDays > 90)    \n";
		s += "    if (balance >= 1000) \n";
		s += "        callCustomer();  \n";
		s += "else sendBill();";

		TokenAssembly ta = new TokenAssembly(s);

		PrettyParser p = new PrettyParser(Dangle.statement());

		List out = p.parseTrees(ta);
		out.forEach(e -> {
			System.out.println("The input parses as:");
			System.out.println("---------------------------");
			System.out.println(e);
		});
	}
}