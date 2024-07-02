package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.Utils.arrayJoin;

import java.util.stream.Stream;

/**
 * 
 * @author u$f
 *
 */
//@see ComparisonExpressionGroup
public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final DBFilter[] filters;
	
	ColumnFilterGroup(LogicalOperator operator, DBFilter... filters) {
		this.operator = operator;
		this.filters = filters;
	}

	@Override
	public String sql(QueryParameterBuilder builder) {
		return "(" + Stream.of(filters)
		.map(o-> o.sql(builder))
		.collect(joining(operator.sql())) + ")";
	}
	
	@Override
	public boolean isAggregation() {
		return Stream.of(filters).anyMatch(DBFilter::isAggregation);
	}

	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		return operator == op 
				? new ColumnFilterGroup(op, arrayJoin(filters, filter))
		        : new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
