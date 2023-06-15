package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface BasicComparator extends DBComparator {
	
	String symbol();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, ()-> "comparartor " + symbol());
		return builder.appendParameter(args[0]) + symbol() + builder.appendParameter(args[1]);
	}
	
	static BasicComparator basicComparator(final String name) {
		return ()-> name;
	}
}