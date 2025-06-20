package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ConstantOperator extends Operator {
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, ConstantOperator.class::getSimpleName);
		query.append(id()); //use parentheses !?
	}
}
