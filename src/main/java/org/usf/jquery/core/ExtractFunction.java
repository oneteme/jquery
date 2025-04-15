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
	default void build(QueryBuilder query, Object... args) {
		requireNArgs(1, args, ExtractFunction.class::getSimpleName);
		query.append(id()).appendParenthesis(
				()-> query.append(field()).append(" FROM ").appendParameter(args[0]));
	}
}
