package org.usf.jquery.core;

import static java.lang.String.format;
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
		if(args[1] instanceof String s) {
			query.appendParameter(args[0])
			.appendSpace().append(id()).appendSpace()
			.appendParameter(wildcardArg(s), true);
		}
		throw new IllegalArgumentException(format("Invalid argument %s for %s", args[1], StringComparator.class.getSimpleName()));
	}
	
	default Object wildcardArg(String o) { //Entry<Srtring, Unary<String>>
		return o;
	}
}
