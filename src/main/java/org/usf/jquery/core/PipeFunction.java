package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface PipeFunction extends FunctionOperator {
	
	@Override
	default void build(QueryBuilder query, Object... args) {
		requireAtLeastNArgs(1, args, PipeFunction.class::getSimpleName);
		query.appendLiteral(args[0]).appendSpace();
		FunctionOperator.super.sql(query, args, 1); //optional partition
	}
}