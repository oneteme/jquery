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
final class WhenCase implements DBObject, Typed {
	
	private final DBFilter filter; //optional
	private final Object value; //then|else

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return DBObject.tryComposeNested(query, groupKeys, filter, value);
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, WhenCase.class::getSimpleName);
		(nonNull(filter) 
				? query.append("WHEN ").append(filter).append(" THEN ") 
				: query.append("ELSE "))
		.appendLiteral(value);
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
