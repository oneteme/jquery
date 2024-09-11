package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface StringComparator extends Comparator {
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(2, args, StringComparator.class::getSimpleName);
		sb.append(ctx.appendParameter(args[0]))
		.spacing(id()).append(ctx.appendParameter(wildcardArg(args[1])));
	}
	
	default Object wildcardArg(Object o) {
		return o;
	}
}
