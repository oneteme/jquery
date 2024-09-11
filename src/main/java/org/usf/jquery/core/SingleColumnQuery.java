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
		if(query.getBuilder().getColumns().size() == 1) {
			this.type = query.getBuilder().getColumns().get(0).getType();
		}
		else{
			throw new IllegalArgumentException("require only one column");
		}
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, SingleColumnQuery.class::getSimpleName);
		sql(sb, ctx);
	}
		
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
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
