package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface BasicComparator extends Comparator {

	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + id() + builder.appendParameter(args[1]);
	}
}