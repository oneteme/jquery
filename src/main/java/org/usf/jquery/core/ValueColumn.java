package org.usf.jquery.core;

import static org.usf.jquery.core.QueryBuilder.formatValue;

import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class ValueColumn implements DBColumn {
	
	private final JDBCType type;
	private final Supplier<Object> supp;
	
	@Override
	public void build(QueryBuilder query) {
		query.append(formatValue(supp.get()));
	}

	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return -1;
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
