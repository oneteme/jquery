package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface AggregationFunction extends DBFunction {

	@Override
	default boolean isAggregation() {
		return true;
	}
	
	static AggregationFunction aggregationFunction(String name) {
		return ()-> name;
	}
	
}
