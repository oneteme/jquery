package org.usf.jquery.core;

import static org.usf.jquery.core.Clause.FILTER;
import static org.usf.jquery.core.QueryContext.formatValue;

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
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.append(formatValue(supp.get()));
	}

	@Override
	public JDBCType getType() {
		return type;
	}

	@Override
	public boolean resolve(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) {
		return builder.getClause() != FILTER; //COLUMN=> !GROUPBY || FILTER=> !HAVING
	}

	@Override
	public void views(Consumer<DBView> cons) {
		//no views
	}

	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
