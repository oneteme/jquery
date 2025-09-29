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
	default void buildOperator(QueryBuilder query, Object... args) {
		requireAtLeastNArgs(1, args, PipeFunction.class::getSimpleName);
		query.appendParameter(args[0]).appendSpace();
		FunctionOperator.super.buildOperator(query, args, 1); //optional partition
	}
}