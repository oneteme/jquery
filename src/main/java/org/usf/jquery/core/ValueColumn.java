package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ValueColumn implements DBColumn, Driven<ValueColumn, Object> {
	
	private final Object value;
	private final JDBCType type;
	private final Adjuster<Object> adjsuter;

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return -1;
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.appendParameter(value, adjsuter);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}
	
	@Override
	public ValueColumn adjuster(Adjuster<Object> adjuster) {
		return new ValueColumn(value, type, adjuster);
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
