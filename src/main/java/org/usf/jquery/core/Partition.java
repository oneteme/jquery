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

	private final DBColumn[] columns;
	private final  DBOrder[] orders;
	
	@Override
	public String sql(QueryVariables builder, Object[] args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		return sql(builder);
	}
	
	String sql(QueryVariables builder) {
		var sb = new SqlStringBuilder(100);
		if(!isEmpty(columns)) {
			sb.append("PARTITION BY ").append(builder.appendLiteralArray(columns));
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(!isEmpty(columns), SPACE)
			.append("ORDER BY ").append(builder.appendLiteralArray(orders));
		}
		return sb.toString();
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) { 
		var r1 = resolveAll(columns, builder);
		var r2 = resolveAll(orders, DBOrder::getColumn, builder);
		return r1 || r2;
	}
	
	@Override
	public void views(Collection<DBView> views) {
		viewsOfNested(views, columns);
		viewsOfNested(views, orders, DBOrder::getColumn);
	}
}
