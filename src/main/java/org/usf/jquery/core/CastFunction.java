package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNArgs;

import java.util.function.IntFunction;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CastFunction extends DBFunction {

	String asType();
	
	@Override
	default String name() {
		return "CAST";
	}
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args, IntFunction<SQLType> indexedType) {
		var n = "VARCHAR".equals(asType()) ? 2 : 1; //require length
		requireNArgs(n, args, ()-> name() + "." + asType());
		return new SqlStringBuilder(name())
		.append("(").append(builder.appendLitteral(args[0], indexedType.apply(0))).append(" AS ").append(asType())
		.appendIf(n == 2, ()-> "(" + builder.appendLitteral(args[1], indexedType.apply(1))+ ")")
		.append(")")
		.toString();
	}

	static CastFunction castFunction(String type) {
		return ()-> type;
	}
	
}
