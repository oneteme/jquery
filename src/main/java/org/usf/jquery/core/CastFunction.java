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
		sb.function(id(), ()-> {
			ctx.appendLiteral(sb, args[0]);
			sb.as(asType());
			if(args.length > 1) {
				sb.parenthesis(()-> ctx.appendLiteralArray(sb, args, 1));
			}
		});
	}
}
