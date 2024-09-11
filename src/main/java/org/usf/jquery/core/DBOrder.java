package org.usf.jquery.core;

import static java.util.Objects.isNull;
import static org.usf.jquery.core.QueryContext.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor
public final class DBOrder implements DBObject, Nested {

	private final DBColumn column;
	private final OrderType order;
	
	public DBOrder(DBColumn column) {
		this(column, null);
	}
	
	@Override
	public String sql(QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		return sql(ctx);
	}

	public String sql(QueryContext ctx) {
		return isNull(order)
				? column.sql(ctx)
				: column.sql(ctx) + SPACE + order.name();
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		return column.resolve(builder);
	}
	
	@Override
	public void views(Collection<DBView> views) {
		column.views(views);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
