package org.usf.jquery.core;

import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.COMA;
import static org.usf.jquery.core.SqlStringBuilder.SPACE;
import static org.usf.jquery.core.Validation.requireNoArgs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
public final class OverClause implements DBObject {

	private final List<DBColumn> partitions = new LinkedList<>();
	private final List<DBOrder> orders = new LinkedList<>();
	
	public OverClause partitions(@NonNull DBColumn... columns) {
		Stream.of(columns).forEach(this.partitions::add);
		return this;
	}

	public OverClause orders(@NonNull DBOrder... orders) {
		Stream.of(orders).forEach(this.orders::add);
		return this;
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		requireNoArgs(args, getClass()::getSimpleName);
		return sql(builder);
	}
	
	public String sql(QueryParameterBuilder builder) {
		var qp = addWithValue(); //no alias
		var sb = new SqlStringBuilder(100).append("OVER(");
		if(!partitions.isEmpty()) {
			sb.append("PARTITION BY ").appendEach(partitions, COMA, o-> o.sql(qp));
		}
		if(!orders.isEmpty()) { //require orders
			sb.appendIf(!partitions.isEmpty(), SPACE)
			.append("ORDER BY ").appendEach(orders, COMA, o-> o.sql(qp));
		}
		return sb.append(")").toString();
	}
}
