package org.usf.jquery.core;

import static java.lang.Math.max;
import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
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
public final class Partition implements DBObject {

	private final Column[] columns;//optional
	private final  Order[] orders; //optional
	
	@Override
	public int compose(QueryComposer query, Consumer<Column> groupKeys) {
		return max(
				DBObject.composeNested(query, groupKeys, columns), 
				DBObject.composeNested(query, groupKeys, orders));
	}
	
	@Override
	public void build(QueryBuilder query, Object... args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		if(!isEmpty(columns)) {
			query.append("PARTITION BY ").appendEach(SCOMA, columns);
		}
		if(!isEmpty(orders)) {
			if(!isEmpty(columns)) {
				query.appendSpace();
			}
			query.append("ORDER BY ").appendEach(SCOMA, orders);
		}
	}
	
	public Partition orders(Order... orders) {
		if(isEmpty(orders)) {
			return this;
		}
		if(isEmpty(this.orders)) {
			return new Partition(columns, orders);
		}
		return new Partition(columns, concat(stream(this.orders), stream(orders)).toArray(Order[]::new));
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
