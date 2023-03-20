package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

@FunctionalInterface
interface BasicComparator extends DBComparator {
	
	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, 2), ()-> name() + " compartor takes 2 parameters");
		return builder.appendParameter(args[0]) + name() + builder.appendParameter(args[1]);
	}
	
	static BasicComparator basicComparator(final String name) {
		return ()-> name;
	}
}