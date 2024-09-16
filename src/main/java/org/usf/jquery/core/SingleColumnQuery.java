package org.usf.jquery.core;

import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public final class SingleColumnQuery implements DBObject, Typed {
	
	private final QueryView query;
	private final JDBCType type;

	SingleColumnQuery(QueryView query) {
		this.query = query;
		var cols = query.getBuilder().getColumns(); 
		if(cols.size() == 1) {
			this.type = cols.iterator().next().getType();
		}
		else{
			throw new IllegalArgumentException("require only one column");
		}
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, SingleColumnQuery.class::getSimpleName);
		query.sql(sb, ctx);
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
