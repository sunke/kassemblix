package com.sjm.examples.cloning;

/*
 * Copyright (c) 1999 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 *  
 * This class will not compile; it just shows that an object 
 * cannot send clone() to another type of object.
 * 
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
public class CannotCloneAnotherType {
/**
 * Just a demo, this will not compile.
 */
public static void main(String[] args) throws CloneNotSupportedException {
	CannotCloneAnotherType ccat = 
		new CannotCloneAnotherType();
		
	ccat.clone(); // this would be Ok.
	
	Integer i = new Integer(42);
	//i.clone(); // will not compile!
}
}
