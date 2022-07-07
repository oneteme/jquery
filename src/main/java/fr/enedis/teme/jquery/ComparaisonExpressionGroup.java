package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static fr.enedis.teme.jquery.SqlStringBuilder.EMPTY_STRING;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import lombok.NonNull;

//@see ColumnFilterGroup
public final class ComparaisonExpressionGroup implements ComparatorExpression {
	
	private final LogicalOperator operator;
	private final List<ComparatorExpression> expressions;
	
	public ComparaisonExpressionGroup(@NonNull LogicalOperator operator, @NonNull ComparatorExpression... expressions) {
		this.operator = operator;
		this.expressions = Stream.of(expressions).collect(toList());
	}
	
	@Override
	public String sql(QueryParameterBuilder arg, Object operand) {
		
		return new SqlStringBuilder(50 * expressions.size())
				.append("(")
				.appendEach(expressions, operator.sql(), e-> e.sql(arg, operand))
				.append(")")
				.toString();
	}

	@Override
	public ComparatorExpression append(LogicalOperator op, ComparatorExpression exp) {
		if(operator == op) {
			expressions.add(exp);
			return this;
		}
		return new ComparaisonExpressionGroup(op, this, exp);
	}
	
	@Override
	public String toString() {
		return sql(addWithValue(), EMPTY_STRING);
	}

}
