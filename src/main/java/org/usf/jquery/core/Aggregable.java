package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Aggregable {
	
	default boolean isAggregation() {
		return false;
	}
	
	static boolean aggregation(Object o) {
		return o instanceof Aggregable && ((Aggregable)o).isAggregation();
	}
}
