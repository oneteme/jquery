package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Store {
	
	StoreMetadata metadata();
		
	Operators operators();
		
	Comparators comparators();
}
