package com.sjm.examples.coffee;

import java.util.*;
/*
 * Copyright (c) 2000 Steven J. Metsker. All Rights Reserved.
 * 
 * Steve Metsker makes no representations or warranties about
 * the fitness of this software for any particular purpose, 
 * including the implied warranty of merchantability.
 */

/**
 * This helper adds a new coffee object to the end of
 * a List of coffees.
 *
 * @author Steven J. Metsker
 *
 * @version 1.0 
 */
 public class NewCoffeeHelper extends Helper {
/**
 * Add a new coffee object to the end of a List of coffees.
 */
public void startElement(Object target) {
	List v = (List) target;
	v.add(new Coffee());
}
}
