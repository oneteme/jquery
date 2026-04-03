package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.JDBCType.typeOf;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
final class WhenCase implements DBObject, Typed {
	
	private final Criteria filter; //optional
	private final Object value; //then|else

	@Override
	public int compose(QueryDeclaration query) {
		var v = value instanceof DBObject c ? c.compose(query) : -1;
		return nonNull(filter)
				? Math.max(v, filter.compose(query))
				: v;
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		(nonNull(filter) 
				? query.append("WHEN ").append(filter).append(" THEN ") 
				: query.append("ELSE "))
		.appendParameter(value);
	}
	
	@Override
	public JDBCType getType() {
		return typeOf(value).orElse(null);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
