package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Nested {
	
	default boolean isAggregation() {
		return false;
	}

	static boolean aggregation(Object o) {
		return o instanceof Nested nes && nes.isAggregation();
	}
}
