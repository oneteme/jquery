package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Nested.viewsOf;
import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

import org.usf.jquery.core.JavaType.Typed;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class WhenCase implements DBObject, Typed, Nested {
	
	private final DBFilter filter; //optional
	private final Object value;

	@Override
	public String sql(QueryVariables qv, Object[] args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
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
	public boolean resolve(QueryBuilder builder) {
		var r1 = nonNull(filter) && filter.resolve(builder);
		var r2 = Nested.resolve(value, builder);
		return r1 || r2;
	}

	
	@Override
	public void views(Collection<DBView> views) {
		if(nonNull(filter)) {
			filter.views(views);
		}
		viewsOf(views, value);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
	public static WhenCase orElse(Object value) {
		return new WhenCase(null, value);
	}
}
