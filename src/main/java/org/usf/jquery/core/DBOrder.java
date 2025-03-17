package org.usf.jquery.core;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

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
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		sql(sb, ctx);
	}

	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		column.sql(sb, ctx);
		if(nonNull(order)) {
			sb.appendSpace().append(order.name());
		}
	}
	
	@Override
	public int columns(QueryBuilder builder, Consumer<DBColumn> groupKeys) {
		return column.columns(builder, groupKeys);
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		column.views(cons);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
