package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ExtractFunction extends FunctionOperator {
	
	String field();
	
	@Override
	default String id() {
		return "EXTRACT";
	}
	
	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireNArgs(1, args, ExtractFunction.class::getSimpleName);
		return id() + "(" + field() + " FROM " + builder.appendLiteral(args[0]) + ")";
	}
}
