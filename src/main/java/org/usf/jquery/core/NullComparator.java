package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface NullComparator extends Comparator {

	@Override
	default String sql(QueryContext ctx, Object[] args) {
		requireNArgs(1, args, NullComparator.class::getSimpleName);
		return ctx.appendParameter(args[0]) + SPACE + id();
	}
}
