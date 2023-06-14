package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.space;
import static org.usf.jquery.core.Utils.hasSize;
import static org.usf.jquery.core.Validation.illegalArgumentIf;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends DBComparator {
	
	String name();

	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		illegalArgumentIf(!hasSize(args, 2),  ()-> name() + " comparator takes 2 parameters");
		return builder.appendString(args[0]) + space(name()) + builder.appendString(args[1]);
	}

	static StringComparator stringComparator(final String name) {
		return ()-> name;
	}
	
}
