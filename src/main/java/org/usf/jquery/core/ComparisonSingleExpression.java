package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.With;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ComparisonSingleExpression implements ComparisonExpression {

	private final Comparator comparator;
	private final Object[] right; //optional
	@With
	private final Adjuster<Object[]> adjuster; //optional

	public ComparisonSingleExpression(Comparator comparator, Object[] right) {
		this(comparator, right, null);
	}	
	
	@Override
	public int compose(QueryComposer query, Consumer<DBColumn> groupKeys) {
		return DBObject.tryComposeNested(query, groupKeys, right);
	}
	
	@Override
	public void build(QueryBuilder query, Object left) {
		var param = new ArrayList<>();
		param.add(left);
		if(nonNull(right)) {
			addAll(param, nonNull(adjuster) ? adjuster.build(query, right) : right);
		}
		comparator.build(query, param.toArray());
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
