package org.usf.jquery.core;

import static java.util.Objects.nonNull;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ValueColumn implements DBColumn {
	
	private final Object value;
	private final JDBCType type;
	private final Adjuster<Object> adjuster; //value adjuster

	public ValueColumn(Object value, JDBCType type) {
		this(value, type, null);
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return -1;
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.appendParameter(nonNull(adjuster) ? adjuster.build(query, value) : value);
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
