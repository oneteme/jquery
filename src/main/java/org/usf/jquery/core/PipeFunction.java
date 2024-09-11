package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
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
		sb.append(ctx.appendLiteral(args[0])).space();
		FunctionOperator.super.sql(sb, ctx, copyOfRange(args, 1, args.length));
	}
}