package org.usf.jquery.core;

import static java.lang.Math.max;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.function.Consumer;

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
	public int columns(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) {
		return max(Nested.resolveColumn(builder, groupKeys, columns), 
				Nested.resolveColumn(builder, groupKeys, orders));
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		Nested.resolveViews(cons, columns);
		Nested.resolveViews(cons, orders);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
