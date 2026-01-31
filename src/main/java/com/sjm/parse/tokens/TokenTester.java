package com.sjm.parse.tokens;

import com.sjm.parse.*;
public class TokenTester extends ParserTester {
/**
 * 
 */
public TokenTester(Parser p) {
	super(p);
}
/**
 * assembly method comment.
 */
protected Assembly assembly(String s) {
	return new TokenAssembly(s);
}
}
