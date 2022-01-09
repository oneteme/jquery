package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;

//@see ColumnFilterGroup
public final class OperatorExpressionGroup implements OperatorExpression {
	
	private final LogicalOperator operator;
	private final List<OperatorExpression> expressions;
	
	public OperatorExpressionGroup(@NonNull LogicalOperator operator, @NonNull OperatorExpression... expressions) {
		this.operator = operator;
		this.expressions = Stream.of(expressions).collect(toList());
	}
	
	@Override
	public String sql(String cn, ParameterHolder ph) {		
		return expressions.stream()
				.map(e-> e instanceof OperatorSingleExpression
					? e.sql(cn, ph)
					: "(" + e.sql(cn, ph)+")")
				.collect(joining(operator.toString()));
	}

	@Override
	public OperatorExpression append(LogicalOperator op, OperatorExpression exp) {
		if(operator == op) {
			expressions.add(exp);
			return this;
		}
		return new OperatorExpressionGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return sql("", addWithValue());
	}

}
