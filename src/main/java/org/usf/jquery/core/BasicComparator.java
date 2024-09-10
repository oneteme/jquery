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
	default String sql(QueryContext ctx, Object[] args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		return ctx.appendParameter(args[0]) + id() + ctx.appendParameter(args[1]);
	}
}