package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CastFunction extends FunctionOperator {

	String asType();
	
	@Override
	default String id() {
		return "CAST";
	}
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireAtLeastNArgs(1, args, CastFunction.class::getSimpleName);
		sb.function(id(), ()->{
			sb.append(ctx.appendLiteral(args[0])).as(asType());
			if(args.length > 1) {
				sb.parenthesis(ctx.appendLiteralArray(args, 1));
			}
		});
	}
}
