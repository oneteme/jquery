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
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireNArgs(1, args, this::id);
		return id() + "(" + field() + " FROM " + builder.appendLitteral(args[0]) + ")";
	}
}
