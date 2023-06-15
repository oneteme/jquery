package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface NullComparator extends DBComparator {

	String name();
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(1, args, ()-> "comparator " + name());
		return builder.appendParameter(args[0]) + SPACE + name();
	}

	static NullComparator nullComparator(final String name) {
		return ()-> name;
	}
}
