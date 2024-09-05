package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.SqlStringBuilder.parenthese;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface InComparator extends Comparator {

	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireAtLeastNArgs(2, args, InComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + SPACE + id() + parenthese(builder.appendArrayParameter(args, 1));
	}
}
