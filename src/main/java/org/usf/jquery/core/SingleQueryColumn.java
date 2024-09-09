package org.usf.jquery.core;

import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

/**
 * 
 * @author u$f
 *
 */
public final class SingleQueryColumn implements DBObject, Typed {
	
	private final QueryView query;
	private final JDBCType type;

	SingleQueryColumn(QueryView query) {
		this.query = query;
		if(query.getBuilder().getColumns().size() == 1) {
			this.type = query.getBuilder().getColumns().get(0).getType();
		}
		else{
			throw new IllegalArgumentException("require only one column");
		}
	}

	@Override
	public String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, SingleQueryColumn.class::getSimpleName);
		return sql(builder);
	}
		
	public String sql(QueryVariables builder) {
		return query.sql(builder);
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
