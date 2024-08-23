package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface CastFunction extends FunctionOperator {

	String asType();
	
	@Override
	default String id() {
		return "CAST";
	}
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		requireAtLeastNArgs(1, args, ()-> id() + "_AS_" + asType());
		var sb = new SqlStringBuilder(id())
				.append("(")
				.append(builder.appendLiteral(args[0])).append(" AS ").append(asType());
		if(args.length > 1) {
			sb.append("(")
			.append(builder.appendLiteralArray(args, 1))
			.append(")");
		}
		return sb.append(")").toString();
	}
}
