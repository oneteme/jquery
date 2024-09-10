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
	default String sql(QueryContext ctx, Object[] args) {
		requireAtLeastNArgs(2, args, InComparator.class::getSimpleName);
		return ctx.appendParameter(args[0]) + SPACE + id() + parenthese(ctx.appendArrayParameter(args, 1));
	}
}
