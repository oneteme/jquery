package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
//@see ComparisonExpressionGroup
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
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
		var gpe = operator == op 
				? new ColumnFilterGroup(op, new ArrayList<>(filters))
				: new ColumnFilterGroup(op, this);
		gpe.filters.add(filter);
		return gpe;
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
	
}
