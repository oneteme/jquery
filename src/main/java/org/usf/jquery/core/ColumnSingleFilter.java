package org.usf.jquery.core;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class ColumnSingleFilter implements DBFilter {

	private final Object left;
	private final ComparisonExpression expression;

	@Override
	public void sql(QueryBuilder query) {
		expression.sql(query, left);
	}

	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return Nested.tryAggregation(query, groupKeys, left, expression);
	}

	@Override
	public ColumnFilterGroup append(LogicalOperator op, DBFilter filter) {
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
