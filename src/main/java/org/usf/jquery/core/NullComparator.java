package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface NullComparator extends Comparator {

	String name();
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(1, args, NullComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + SPACE + name();
	}
}
