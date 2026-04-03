package org.usf.jquery.core;

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
	public int compose(QueryDeclaration query) {
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
