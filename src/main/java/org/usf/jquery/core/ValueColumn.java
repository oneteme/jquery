package org.usf.jquery.core;

import static org.usf.jquery.core.QueryContext.formatValue;

import java.util.Collection;
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
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.append(formatValue(supp.get()));
	}

	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public boolean resolve(QueryBuilder builder) {
		return false;
	}

	@Override
	public void views(Collection<DBView> views) {
		//do nothing
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
