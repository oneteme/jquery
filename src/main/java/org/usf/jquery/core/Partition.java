package org.usf.jquery.core;

import static java.lang.Math.max;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.Collection;

/**
 * 
 * @author u$f
 *
 */
public final class Partition implements QueryPart {

	private final Collection<Column> columns;//optional
	private final Collection<Order>   orders; //optional
	
	public Partition(Collection<Column> columns, Collection<Order> orders) {
		if(isEmpty(columns) && isEmpty(orders)) {
			throw new ComposeException("Partition requires at least one column or order");
		}
		this.columns = columns;
		this.orders = orders;
	}
	
	@Override
	public int prepare(QueryAnalyzer manifest) {
		return max(manifest.analyzeNested(columns), manifest.analyzeNested(orders));
	}
	
	@Override
	public void build(SqlBuilder builder, Object... args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		if(!isEmpty(columns)) {
			builder.append("PARTITION BY ").appendEach(SqlBuilder.SCOMA, columns);
		}
		if(!isEmpty(orders)) {
			if(!isEmpty(columns)) {
				builder.appendSpace();
			}
			builder.append("ORDER BY ").appendEach(SqlBuilder.SCOMA, orders);
		}
	}
	
	@Override
	public String toString() {
		return QueryPart.toSQL(this);
	}
}
