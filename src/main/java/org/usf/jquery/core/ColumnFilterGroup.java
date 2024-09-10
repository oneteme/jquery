package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Nested.resolveAll;
import static org.usf.jquery.core.Nested.viewsOfNested;
import static org.usf.jquery.core.QueryContext.addWithValue;
import static org.usf.jquery.core.Utils.appendLast;
import static org.usf.jquery.core.Validation.requireAtLeastNArgs;

import java.util.Collection;
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
		this.filters = requireAtLeastNArgs(1, filters, ColumnFilterGroup.class::getSimpleName);
	}

	@Override
	public String sql(QueryContext ctx) {
		return Stream.of(filters)
		.map(o-> o.sql(ctx))
		.collect(joining(operator.sql(), "(", ")"));
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
		return sql(addWithValue());
	}
}
