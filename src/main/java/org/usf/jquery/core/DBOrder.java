package org.usf.jquery.core;

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
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, DBOrder.class::getSimpleName);
		sql(sb, ctx);
	}

	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		column.sql(sb, ctx);
		sb.runIfNonNull(order, o-> sb.space().append(o.name()));
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
		return DBObject.toSQL(this);
	}
}
