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
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		expression.sql(sb, ctx, left);
	}

	@Override
	public int columns(QueryBuilder builder, Consumer<DBColumn> groupKeys) {
		return Nested.tryResolveColumn(builder, groupKeys, left, expression);
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		Nested.tryResolveViews(cons, left, expression);
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
