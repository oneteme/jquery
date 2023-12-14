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
	
	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, String.class::getSimpleName);
		return builder.appendString(args[0]) + space(name()) + builder.appendString(args[1]);
	}

	static StringComparator stringComparator(final String name) {
		return ()-> name;
	}
	
}
