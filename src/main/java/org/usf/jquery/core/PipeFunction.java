package org.usf.jquery.core;

import static java.util.Arrays.copyOfRange;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface PipeFunction extends FunctionOperator {
	
	@Override
	default String sql(QueryVariables builder, Object[] args) {
		requireAtLeastNArgs(1, args, PipeFunction.class::getSimpleName);
		return builder.appendLiteral(args[0]) + SPACE 
				+ FunctionOperator.super.sql(builder, copyOfRange(args, 1, args.length));
	}
}