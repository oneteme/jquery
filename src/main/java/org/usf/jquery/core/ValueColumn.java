package org.usf.jquery.core;

import static org.usf.jquery.core.QueryBuilder.formatValue;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ValueColumn implements DBColumn, DrivenObject<Object> {
	
	private final JDBCType type;
	private final Object value;

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return -1;
	}
	
	@Override
	public void build(QueryBuilder query) {
		query.append(formatValue(adjust(query, value)));
	}
	
	@Override
	public Object adjust(QueryBuilder query, Object value) {
		return value;
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
