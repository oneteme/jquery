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
	default String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, ConstantOperator.class::getSimpleName);
		return id(); //use parentheses !?
	}
}
