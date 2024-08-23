package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.aggregation;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ColumnSingleFilter implements DBFilter {

	private final Object left;
	private final ComparisonExpression expression;

	@Override
	public String sql(QueryParameterBuilder ph) {
		return expression.sql(ph, left);
	}
	
	@Override
	public boolean isAggregation() {
		return aggregation(left) || expression.isAggregation();
	}

	@Override
	public ColumnFilterGroup append(LogicalOperator op, DBFilter filter) {
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
