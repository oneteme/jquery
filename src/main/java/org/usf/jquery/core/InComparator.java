package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface InComparator extends Comparator {

	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireAtLeastNArgs(2, args, InComparator.class::getSimpleName);
		ctx.appendParameter(sb, args[0]);
		sb.space().function(id(), ()-> ctx.appendArrayParameter(sb, args, 1));
	}
}
