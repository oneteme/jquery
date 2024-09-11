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
	default void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, ConstantOperator.class::getSimpleName);
		sb.append(id()); //use parentheses !?
	}
}
