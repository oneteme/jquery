package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface NestedSql {
	
	default boolean isAggregation() {
		return false;
	}
	
	static boolean aggregation(Object o) {
		return o instanceof NestedSql && ((NestedSql)o).isAggregation();
	}

}
