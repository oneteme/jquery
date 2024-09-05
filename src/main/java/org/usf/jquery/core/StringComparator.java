package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.space;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends Comparator {
	
	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNArgs(2, args, StringComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + space(id()) + builder.appendParameter(wildcardArg(args[1]));
	}
	
	default Object wildcardArg(Object o) {
		return o;
	}
}
