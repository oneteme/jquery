package org.usf.jquery.core;

import static org.usf.jquery.core.Nested.tryResolve;
import static org.usf.jquery.core.Nested.viewsOf;
import static org.usf.jquery.core.QueryVariables.addWithValue;

import java.util.Collection;

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
	public String sql(QueryVariables ph) {
		return expression.sql(ph, left);
	}

	@Override
	public boolean resolve(QueryBuilder builder) {
		var res1 = tryResolve(left, builder);
		var res2 = expression.resolve(builder);
		return res1 || res2;
	}
	
	@Override
	public void views(Collection<DBView> views) {
		viewsOf(views, left);
		expression.views(views);
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
