package org.usf.jquery.core;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.usf.jquery.core.QueryParameterBuilder.addWithValue;
import static org.usf.jquery.core.SqlStringBuilder.EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import lombok.NonNull;

/**
 * 
 * @author u$f
 *
 */
//@see ColumnFilterGroup
public final class ComparisonExpressionGroup implements ComparisonExpression {
	
	private final LogicalOperator operator;
	private final Collection<ComparisonExpression> expressions;
	
	public ComparisonExpressionGroup(@NonNull LogicalOperator operator, ComparisonExpression... expressions) {
		this.operator = operator;
		this.expressions = expressions == null 
				? new ArrayList<>()
				: Stream.of(expressions).collect(toList());
	}
	
	@Override
	public String sql(QueryParameterBuilder builder, Object operand) {
		return "(" + expressions.stream().map(o-> o.sql(builder, operand)).collect(joining(operator.sql())) + ")";
	}
	
	@Override
	public boolean isAggregation() {
		return expressions.stream().anyMatch(ComparisonExpression::isAggregation);
	}

	@Override
	public ComparisonExpression append(LogicalOperator op, ComparisonExpression exp) {
		if(operator == op) {
			expressions.add(exp);
			return this;
		}
		return new ComparisonExpressionGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), EMPTY);
	}

}
