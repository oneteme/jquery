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
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(3, args, RangeComparator.class::getSimpleName);
		sb.append(ctx.appendParameter(args[0]))
		.spacing(id())
		.append(ctx.appendParameter(args[1]))
		.append(AND.sql()).append(ctx.appendParameter(args[2]));
	}
}
