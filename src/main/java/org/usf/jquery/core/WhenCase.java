package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Nested.tryResolve;
import static org.usf.jquery.core.Nested.viewsOf;
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
	private final Object value; //then|else

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		sql(sb, ctx);
	}
	
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		if(nonNull(filter)) {
			sb.append("WHEN ");
			filter.sql(sb, ctx);
			sb.append(" THEN ");
		}
		else {
			sb.append("ELSE ");
		}
		ctx.appendLiteral(sb, value);
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
		return DBObject.toSQL(this);
	}
}
