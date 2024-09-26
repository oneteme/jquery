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
		ctx.appendParameter(sb, args[0]);
		sb.spacing(id());
		ctx.appendParameter(sb, args[1]);
		sb.append(AND.sql());
		ctx.appendParameter(sb, args[2]);
	}
}
