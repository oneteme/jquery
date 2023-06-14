package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

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
		illegalArgumentIf(!hasSize(args, 2), ()-> symbol() + " compartor takes 2 parameters");
		return builder.appendParameter(args[0]) + symbol() + builder.appendParameter(args[1]);
	}
	
	static BasicComparator basicComparator(final String name) {
		return ()-> name;
	}
}