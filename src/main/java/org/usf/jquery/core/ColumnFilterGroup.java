package org.usf.jquery.core;

import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.function.Consumer;

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
		this.filters = requireAtLeastNArgs(1, filters, ColumnFilterGroup.class::getSimpleName);
	}

	@Override
	public void sql(SqlStringBuilder sb, QueryContext ctx) {
		sb.parenthesis(()->
			sb.runForeach(filters, operator.sql(), o-> o.sql(sb, ctx)));
	}

	@Override
	public int columns(QueryBuilder builder, Consumer<? super DBColumn> groupKeys) {
		return Nested.resolveColumn(builder, groupKeys, filters);
	}
	
	@Override
	public void views(Consumer<DBView> cons) {
		Nested.resolveViews(cons, filters);
	}
	
	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		return operator == op 
				? new ColumnFilterGroup(op, appendLast(filters, filter))
		        : new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return DBObject.toSQL(this);
	}
}
