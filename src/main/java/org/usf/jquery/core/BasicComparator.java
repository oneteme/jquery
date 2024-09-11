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
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(2, args, BasicComparator.class::getSimpleName);
		sb.append(ctx.appendParameter(args[0])).append(id()).append(ctx.appendParameter(args[1]));
	}
}