package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class DBOrder implements DBObject {

	private final DBColumn column;
	private final String order;
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		return sql(builder);
	}

	public String sql(QueryParameterBuilder builder) {
		return isNull(order) 
				? column.sql(builder) 
				: column.sql(builder) + SPACE + order;
	}
	
}
