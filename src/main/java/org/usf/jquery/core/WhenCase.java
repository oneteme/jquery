package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

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
	public int declare(RequestComposer builder, Consumer<DBColumn> groupKeys) {
		return Nested.tryAggregation(builder, groupKeys, filter, value);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
