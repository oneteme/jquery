package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

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
	public void sql(SqlStringBuilder sb, QueryContext ctx, Object left) {
		var param = new ArrayList<>();
		param.add(left);
		if(nonNull(right)) {
			addAll(param, right);
		}
		comparator.sql(sb, ctx, param.toArray());
	}
	
	@Override
	public int declare(RequestComposer builder, Consumer<DBColumn> groupKeys) {
		return Nested.tryAggregation(builder, groupKeys, right);
	}

	@Override
	public ComparisonExpression append(LogicalOperator op, ComparisonExpression exp) {
		return new ComparisonExpressionGroup(op, this, exp);
	}

	@Override
	public String toString() {
		var args = new Object[]{null}; //unknown type
		return DBObject.toSQL(this, args);
	}	
}
