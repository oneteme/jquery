package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface RangeComparator extends Comparator {

	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNArgs(3, args, RangeComparator.class::getSimpleName);
		return builder.appendParameter(args[0]) + SPACE + id() + 
				SPACE + builder.appendParameter(args[1]) + 
				AND.sql() + builder.appendParameter(args[2]);
	}
}