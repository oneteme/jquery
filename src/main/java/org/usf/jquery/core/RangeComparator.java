package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface RangeComparator extends Comparator {

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(3, args, RangeComparator.class::getSimpleName);
		query.appendParameter(args[0])
		.appendSpace().append(id()).appendSpace()
		.appendParameter(args[1]).append(AND.sql()).appendParameter(args[2]);
	}
}
