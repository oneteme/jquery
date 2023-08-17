package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface WindowFunction extends DBFunction {
	
	static WindowFunction windowFunction(String name) {
		return ()-> name;
	}
	
}
