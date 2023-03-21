package org.usf.jquery.core;

import static org.usf.jquery.core.NestedSql.aggregation;
import static java.lang.reflect.Array.get;
import static java.lang.reflect.Array.getLength;
import static java.util.stream.IntStream.range;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;

import java.util.LinkedList;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ComparisonSingleExpression implements ComparatorExpression {

	private final DBComparator comparator;
	private final Object right; //null|array|any
	
	@Override
	public String sql(QueryParameterBuilder builder, Object left) {
		var param = new LinkedList<>();
		param.add(left);
		if(right != null) {
			if(right.getClass().isArray()) {
				range(0, getLength(right))
				.forEach(i-> param.add(get(right, i)));
			}
			else {
				param.add(right);
			}
		}
		return comparator.sql(builder, param.toArray());
	}
	
	@Override
	public boolean isAggregation() {
		return aggregation(right);
	}
	
	@Override
	public ComparatorExpression append(LogicalOperator op, ComparatorExpression exp) {
		return new ComparaisonExpressionGroup(op, this, exp);
	}

	@Override
	public String toString() {
		return sql(addWithValue(), new Object[] {EMPTY});
	}
}
