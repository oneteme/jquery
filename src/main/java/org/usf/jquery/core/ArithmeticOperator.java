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
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(2, args, ArithmeticOperator.class::getSimpleName);
		return builder.appendLitteral(args[0]) + id() + builder.appendLitteral(args[1]);
	}
}
