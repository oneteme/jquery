package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface WindowFunction extends DBFunction {
	
	static WindowFunction windowFunction(String name) {
		return ()-> name;
	}
	
}
