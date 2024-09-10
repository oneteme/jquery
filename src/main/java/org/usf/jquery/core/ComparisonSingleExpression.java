package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Nested.tryResolveAll;
import static org.usf.jquery.core.Nested.viewsOfAll;
import static org.usf.jquery.core.QueryContext.addWithValue;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ComparisonSingleExpression implements ComparisonExpression {

	private final Comparator comparator;
	private final Object[] right; //optional
	
	@Override
	public String sql(QueryContext ctx, Object left) {
		var param = new ArrayList<>();
		param.add(left);
		if(nonNull(right)) {
			addAll(param, right);
		}
		return comparator.sql(ctx, param.toArray());
	}
	
	@Override
	public boolean resolve(QueryBuilder builder) {
		return tryResolveAll(builder, right);
	}

	@Override
	public void views(Collection<DBView> views) {
		viewsOfAll(views, right);
	}
	
	@Override
	public ComparisonExpression append(LogicalOperator op, ComparisonExpression exp) {
		return new ComparisonExpressionGroup(op, this, exp);
	}

	@Override
	public String toString() {
		return sql(addWithValue(), "<left>");
	}	
}
