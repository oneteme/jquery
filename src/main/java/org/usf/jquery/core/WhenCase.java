package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Nested.tryResolve;
import static org.usf.jquery.core.Nested.viewsOf;
import static org.usf.jquery.core.QueryContext.addWithValue;
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
	public String sql(QueryContext qv, Object[] args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		return sql(qv);
	}
	
	public String sql(QueryContext ctx) {
		var sb = new StringBuilder(50);
		sb = nonNull(filter)
				? sb.append("WHEN ")
						.append(filter.sql(ctx))
						.append(" THEN ")
				: sb.append("ELSE ");
		return sb.append(ctx.appendLiteral(value)).toString();
	}
	
	@Override
	public JDBCType getType() {
		return typeOf(value).orElse(null);
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		var r1 = nonNull(filter) && filter.resolve(builder);
		var r2 = tryResolve(value, builder);
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
}
