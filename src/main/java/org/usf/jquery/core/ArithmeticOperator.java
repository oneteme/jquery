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
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(2, args, ArithmeticException.class::getSimpleName);
		query.parenthesis(()->
			query.appendLiteral(args[0]).append(id()).appendLiteral(args[1]));
	}
}
