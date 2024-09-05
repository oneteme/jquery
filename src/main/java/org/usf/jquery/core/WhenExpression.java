package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class WhenExpression implements DBExpression, Typed {
	
	private final DBFilter filter;
	private final Object value;

	@Override
	public String sql(QueryVariables qv, Object[] args) {
		requireNoArgs(args, WhenExpression.class::getSimpleName);
		return sql(qv);
	}
	
	public String sql(QueryVariables qv) {
		var sb = new StringBuilder(50);
		sb = isNull(filter)
				? sb.append("ELSE ")
				: sb.append("WHEN ")
				.append(filter.sql(qv))
				.append(" THEN ");
		return sb.append(qv.appendLiteral(value)).toString();
	}
	
	@Override
	public JDBCType getType() {
		return typeOf(value).orElse(null);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
	public static WhenExpression orElse(Object value) {
		return new WhenExpression(null, value);
	}
}
