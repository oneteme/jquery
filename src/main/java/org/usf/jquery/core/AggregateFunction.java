package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface AggregateFunction extends WindowFunction {

	@Override
	default boolean isAggregation() {
		return true;
	}
}
