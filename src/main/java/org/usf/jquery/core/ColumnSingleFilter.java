package org.usf.jquery.core;

import static org.usf.jquery.core.LogicalOperator.AND;
import static org.usf.jquery.core.LogicalOperator.OR;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ColumnSingleFilter implements DBFilter {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final ComparatorExpression expression;

	@Override
	public String sql(QueryParameterBuilder ph) {
		return expression.sql(ph, column);
	}

	@Override
	public ColumnFilterGroup append(LogicalOperator op, DBFilter filter) {
		return new ColumnFilterGroup(op, this, filter);
	}
	
	public DBFilter and(ComparatorExpression exp) {
		return append(AND, exp);
	}
	
	public DBFilter or(ComparatorExpression exp) {
		return append(OR, exp);
	}

	public ColumnSingleFilter append(LogicalOperator op, ComparatorExpression exp) {
		var nex = expression.append(op, exp); //@see OperatorExpressionGroup
		return nex == exp ? this : new ColumnSingleFilter(column, nex);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue());
	}
}
