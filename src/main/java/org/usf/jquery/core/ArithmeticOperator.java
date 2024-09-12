package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
interface ArithmeticOperator extends Operator {
	
	@Override
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNArgs(2, args, ArithmeticException.class::getSimpleName);
		sb.parenthesis(()->{
			ctx.appendLiteral(sb, args[0]); 
			sb.append(id());
			ctx.appendLiteral(sb, args[1]);
		});
	}
}
