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
	default String sql(QueryContext ctx, Object[] args) {
		requireNArgs(2, args, ArithmeticException.class::getSimpleName);
		return "(" + ctx.appendLiteral(args[0]) + id() + ctx.appendLiteral(args[1]) + ")";
	}
}
