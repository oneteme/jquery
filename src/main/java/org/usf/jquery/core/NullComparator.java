package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

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
		illegalArgumentIf(!hasSize(args, 1), ()-> name() + " comparator takes one parameter");
		return builder.appendParameter(args[0]) + SPACE + name();
	}

	static NullComparator nullComparator(final String name) {
		return ()-> name;
	}
}
