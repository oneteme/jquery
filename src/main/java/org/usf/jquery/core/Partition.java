package org.usf.jquery.core;

import static java.lang.Math.max;
import static org.usf.jquery.core.SqlStringBuilder.SCOMA;
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
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		if(!isEmpty(columns)) {
			query.append("PARTITION BY ").append(SCOMA, columns);
		}
		if(!isEmpty(orders)) { //require orders
			if(!isEmpty(columns)) {
				query.appendSpace();
			}
			query.append("ORDER BY ").append(SCOMA, orders);
		}
	}
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return max(
				Nested.aggregation(query, groupKeys, columns), 
				Nested.aggregation(query, groupKeys, orders));
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
