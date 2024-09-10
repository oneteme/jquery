package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.space;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends Comparator {
	
	@Override
	default String sql(QueryContext ctx, Object[] args) {
		requireNArgs(2, args, StringComparator.class::getSimpleName);
		return ctx.appendParameter(args[0]) + space(id()) + ctx.appendParameter(wildcardArg(args[1]));
	}
	
	default Object wildcardArg(Object o) {
		return o;
	}
}
