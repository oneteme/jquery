package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.COMA;
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
		var sb = new SqlStringBuilder(id()).append("(")
				.append(builder.appendLitteral(args[0])).append(" AS ").append(asType());
		if(args.length > 1) {
			sb.append("(")
			.append(builder.appendLitteral(args[1]));
			for(int i=2; i<args.length; i++) {
				sb.append(COMA).append(builder.appendLitteral(args[i]));
			}
			sb.append(")");
		}
		return sb.append(")").toString();
	}
}
