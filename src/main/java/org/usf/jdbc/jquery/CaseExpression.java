package org.usf.jdbc.jquery;

import static org.usf.jdbc.jquery.QueryParameterBuilder.addWithValue;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class CaseExpression implements DBExpression {
	
	private final DBFilter filter;
	private final Object value;
	
	@Override
	public String sql(QueryParameterBuilder arg, Object operand) {
		var sb = new StringBuilder(50);
		sb = filter == null
				? sb.append("ELSE ")
				: sb.append("WHEN ").append(filter.sql(arg)).append(" THEN ");
		return sb.append(arg.appendParameter(value)).toString();
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), null);
	}
	
	public static CaseExpression orElse(Object value) {
		return new CaseExpression(null, value);
	}
	
}
