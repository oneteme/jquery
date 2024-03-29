package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class WhenExpression implements DBExpression {
	
	private final DBFilter filter;
	private final Object value;

	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, WhenExpression.class::getSimpleName);
		return sql(builder);
	}
	
	public String sql(QueryParameterBuilder arg) {
		var sb = new StringBuilder(50);
		sb = filter == null
				? sb.append("ELSE ")
				: sb.append("WHEN ")
				.append(filter.sql(arg))
				.append(" THEN ");
		return sb.append(arg.appendParameter(value)).toString();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), null);
	}
	
	public static WhenExpression orElse(Object value) {
		return new WhenExpression(null, value);
	}
	
}
