package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface NullComparator extends Comparator {

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(1, args, NullComparator.class::getSimpleName);
		query.appendParameter(args[0]).appendSpace().append(id());
	}
}
