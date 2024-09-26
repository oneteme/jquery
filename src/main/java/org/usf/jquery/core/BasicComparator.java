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
		ctx.appendParameter(sb, args[0]);
		sb.append(id());
		ctx.appendParameter(sb, args[1]);
	}
}