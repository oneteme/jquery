package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.Nested.viewsOfNested;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

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
		sql(sb, ctx);
	}
	
	void sql(SqlStringBuilder sb, QueryContext ctx) {
		if(!isEmpty(columns)) {
			sb.append("PARTITION BY ").append(ctx.appendLiteralArray(columns));
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(!isEmpty(columns), SPACE)
			.append("ORDER BY ").append(ctx.appendLiteralArray(orders));
		}
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) { 
		var r1 = resolveAll(columns, builder);
		var r2 = resolveAll(orders, builder);
		return r1 || r2;
	}
	
	@Override
	public void views(Collection<DBView> views) {
		viewsOfNested(views, columns);
		viewsOfNested(views, orders);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
