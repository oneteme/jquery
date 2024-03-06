package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
//@see ComparisonExpressionGroup
public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final Collection<DBFilter> filters;

	ColumnFilterGroup(@NonNull LogicalOperator operator, DBFilter... filters) {//assert length > 1
		this.operator = operator;
		this.filters = filters == null 
				? new LinkedList<>()
				: Stream.of(filters).collect(toList());
	}
	
	@Override
	public String sql(QueryParameterBuilder builder) {
		return "(" + filters.stream().map(o-> o.sql(builder)).collect(joining(operator.sql())) + ")";
	}
	
	@Override
	public boolean isAggregation() {
		return filters.stream().anyMatch(DBFilter::isAggregation);
	}

	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		if(operator == op) {
			filters.add(filter);
			return this;
		}
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
}
