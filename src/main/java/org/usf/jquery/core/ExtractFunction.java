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
	default void sql(SqlStringBuilder sb, QueryContext builder, Object[] args) {
		requireNArgs(1, args, ExtractFunction.class::getSimpleName);
		sb.function(id(), ()->{
			sb.append(field()).append(" FROM ");
			builder.appendLiteral(sb, args[0]);
		});
	}
}
