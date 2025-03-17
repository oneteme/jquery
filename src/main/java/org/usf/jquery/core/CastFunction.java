package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Map;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CastFunction extends FunctionOperator {

	String type();
	
	@Override
	default String id() {
		return "CAST";
	}
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireAtLeastNArgs(1, args, CastFunction.class::getSimpleName);
		sb.function(id(), ()-> {
			ctx.appendLiteral(sb, args[0]);
			sb.appendAs(type());
			if(args.length > 1) { //varchar | decimal
				sb.parenthesis(()-> ctx.appendLiteralArray(sb, args, 1));
			}
		});
	}
}
