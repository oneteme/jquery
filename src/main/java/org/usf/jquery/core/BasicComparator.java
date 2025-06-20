package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface BasicComparator extends Comparator {

	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		query.appendParameter(args[0]).append(id()).appendParameter(args[1], true);
	}
}