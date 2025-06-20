package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends Comparator {
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(2, args, StringComparator.class::getSimpleName);
		query.appendParameter(args[0])
		.appendSpace().append(id()).appendSpace()
		.appendParameter(wildcardArg(args[1]), true);
	}
	
	default Object wildcardArg(Object o) { //Entry<Srtring, Unary<String>>
		return o;
	}
}
