package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface BasicComparator extends Comparator {
	
	String symbol();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + symbol() + builder.appendParameter(args[1]);
	}
}