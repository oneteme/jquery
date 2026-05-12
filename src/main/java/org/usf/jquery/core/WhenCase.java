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
	private final Object result; //then|else

	@Override
	public int prepare(QueryManifest query) {
		var v = result instanceof DBObject c ? c.prepare(query) : SCALAR;
		return nonNull(filter)
				? Math.max(v, filter.prepare(query))
				: v;
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		(nonNull(filter) 
				? query.append("WHEN ").append(filter).append(" THEN ") 
				: query.append("ELSE "))
		.appendParameter(result);
	}
	
	@Override
	public JDBCType getType() {
		return typeOf(result).orElse(null);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
