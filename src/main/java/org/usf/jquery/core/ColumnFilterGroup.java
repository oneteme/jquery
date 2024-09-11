package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.Nested.viewsOfNested;
import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Collection;

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
			sb.appendEach(filters, operator.sql(), o-> o.sql(sb, ctx)));
	}
		
	@Override
	public boolean resolve(QueryBuilder ctx) {
		return resolveAll(filters, ctx);
	}
	
	@Override
	public void views(Collection<DBView> views) {
		viewsOfNested(views, filters);
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
