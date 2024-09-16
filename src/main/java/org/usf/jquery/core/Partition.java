package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class Partition implements DBObject, Nested {

	private final DBColumn[] columns;//optional
	private final  DBOrder[] orders; //optional
	
	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object[] args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		if(!isEmpty(columns)) {
			sb.append("PARTITION BY ");
			ctx.appendLiteralArray(sb, columns);
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(!isEmpty(columns), SPACE);
			sb.append("ORDER BY ");
			ctx.appendLiteralArray(sb, orders);
		}
	}
	
	@Override
	public boolean resolve(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) { 
		if(!isEmpty(columns)) {
			Stream.of(columns)
			.filter(c-> !c.resolve(builder, groupKeys))
			.forEach(groupKeys);
		}
		if(!isEmpty(orders)) {
			Stream.of(orders)
			.filter(c-> !c.resolve(builder, groupKeys))
			.map(DBOrder::getColumn)
			.forEach(groupKeys);
		}
		return true; //!grouping keys 
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		Nested.viewsOf(cons, (Object[])columns);
		Nested.viewsOf(cons, (Object[])orders);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
