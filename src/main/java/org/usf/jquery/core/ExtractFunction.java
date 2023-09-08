package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.function.IntFunction;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface ExtractFunction extends DBFunction {
	
	String field();
	
	@Override
	default String name() {
		return "EXTRACT";
	}
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args, IntFunction<SQLType> indexedType) {
		requireNArgs(1, args, this::name);
		return name() + "(" + field() + " FROM " + 
				builder.appendLitteral(args[0], indexedType.apply(0)) + ")";
	}

	static ExtractFunction extractFunction(String type) {
		return ()-> type;
	}
	
}
