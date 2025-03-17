package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface PipeFunction extends FunctionOperator {
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireAtLeastNArgs(1, args, PipeFunction.class::getSimpleName);
		ctx.appendLiteral(sb, args[0]);
		sb.appendSpace();
		FunctionOperator.super.sql(sb, ctx, args, 1); //optional partition
	}
}