package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ValueColumn implements Column {
	
	private final Object value;
	private final JDBCType type;

	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return -1;
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.appendParameter(value);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
