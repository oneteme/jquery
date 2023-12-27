package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class DBOrder implements DBObject {

	private final DBColumn column;
	private final Order order;
	
	public DBOrder(DBColumn column) {
		this(column, null);
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		return sql(builder);
	}

	public String sql(QueryParameterBuilder builder) {
		return isNull(order)
				? column.sql(builder)
				: column.sql(builder) + SPACE + order.name();
	}
	
}
