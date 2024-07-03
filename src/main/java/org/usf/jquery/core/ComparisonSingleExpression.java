package org.usf.jquery.core;

import static java.util.Collections.addAll;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;

import java.util.ArrayList;
import java.util.stream.Stream;

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
	private final Object[] right; //nullable
	
	@Override
	public String sql(QueryParameterBuilder builder, Object left) {
		var param = new ArrayList<>();
		param.add(left);
		if(nonNull(right)) {
			addAll(param, right);
		}
		return comparator.sql(builder, param.toArray());
	}
	
	@Override
	public boolean isAggregation() {
		return nonNull(right) && Stream.of(right).anyMatch(Aggregable::aggregation);
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
