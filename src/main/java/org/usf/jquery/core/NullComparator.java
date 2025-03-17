package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface NullComparator extends Comparator {

	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(1, args, NullComparator.class::getSimpleName);
		ctx.appendParameter(sb, args[0]);
		sb.appendSpace().append(id());
	}
}
