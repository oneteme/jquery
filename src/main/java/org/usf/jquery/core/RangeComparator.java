package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface RangeComparator extends Comparator {

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(3, args, RangeComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + " " + id() + 
				" " + builder.appendParameter(args[1]) + 
				" AND " + builder.appendParameter(args[2]);
	}
}
