package org.usf.jquery.core;

import static org.usf.jquery.core.QueryVariables.addWithValue;
import static org.usf.jquery.core.Validation.requireNArgs;
import static org.usf.jquery.core.Validation.requireNoArgs;

import org.usf.jquery.core.JavaType.Typed;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class SingleQueryColumn implements DBObject, Typed {
	
	private final QueryView query;
	@Getter
	private final JDBCType type;

	@Override
	public String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, SingleQueryColumn.class::getSimpleName);
		return sql(builder);
	}
		
	public String sql(QueryVariables builder) {
		requireNArgs(1, query.getBuilder().getColumns().toArray(), SingleQueryColumn.class::getSimpleName);
		return query.sql(builder);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
