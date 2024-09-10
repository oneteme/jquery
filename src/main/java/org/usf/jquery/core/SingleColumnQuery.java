package org.usf.jquery.core;

import static org.usf.jquery.core.QueryContext.addWithValue;
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
	public String sql(QueryContext ctx, Object[] args) {
		requireNoArgs(args, SingleColumnQuery.class::getSimpleName);
		return sql(ctx);
	}
		
	public String sql(QueryContext ctx) {
		return query.sql(ctx);
	}
	
	@Override
	public JDBCType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
