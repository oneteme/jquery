package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ArithmeticOperator extends Operator {
	
	@Override
	default void buildOperator(QueryBuilder query, Object... args) {
		requireNArgs(2, args, ArithmeticException.class::getSimpleName);
		query.appendParenthesis(()->
			query.appendParameter(args[0]).append(id()).appendParameter(args[1]));
	}
}
