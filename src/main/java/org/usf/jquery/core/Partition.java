package org.usf.jquery.core;

import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Utils.isEmpty;
import static org.usf.jquery.core.Validation.requireNoArgs;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public final class Partition implements DBObject {

	private final DBColumn[] columns;
	private DBOrder[] orders;
	
	public Partition() {
		this(null);
	}
	
	public void orders(DBOrder[] orders) {
		this.orders = orders;
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, Partition.class::getSimpleName);
		return sql(builder);
	}
	
	String sql(QueryParameterBuilder builder) {
		var sb = new SqlStringBuilder(100);
		if(!isEmpty(columns)) {
			sb.append("PARTITION BY ").append(builder.appendLitteralArray(columns));
		}
		if(!isEmpty(orders)) { //require orders
			sb.appendIf(!isEmpty(columns), SPACE)
			.append("ORDER BY ").append(builder.appendLitteralArray(orders));
		}
		return sb.toString();
	}
}
