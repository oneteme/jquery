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
final class WhenCase implements QueryPart, Typed {
	
	private final Criteria criteria; //optional
	private final Object result; //then|else

	@Override
	public int prepare(QueryAnalyzer analyzer) {
		var v = analyzer.tryAnalyzeNested(result);
		return nonNull(criteria) ? Math.max(v, criteria.prepare(analyzer)) : v;
	}
	
	@Override
	public void build(SqlBuilder builder, Object... args) {
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
		return QueryPart.toSQL(this);
	}
}
