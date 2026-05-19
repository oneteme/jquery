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
	
	private final Criteria criteria; //optional
	private final Object result; //then|else

	@Override
	public int prepare(QueryManifest manifest) {
		var v = result instanceof DBObject c ? c.prepare(manifest) : SCALAR;
		return nonNull(criteria) ? Math.max(v, criteria.prepare(manifest)) : v;
	}
	
	@Override
	public void build(QueryBuilder builder, Object... args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		(nonNull(criteria) 
				? builder.append("WHEN ").append(criteria).append(" THEN ") 
				: builder.append("ELSE "))
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
