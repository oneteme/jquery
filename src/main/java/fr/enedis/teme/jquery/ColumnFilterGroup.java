package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

import lombok.NonNull;

//@see OperatorExpressionGroup
public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final Collection<DBFilter> expressions;

	public ColumnFilterGroup(@NonNull LogicalOperator operator, DBFilter... expression) {//assert length > 1
		this.operator = operator;
		this.expressions = expression == null 
				? new LinkedList<>()
				: Stream.of(expression).collect(toList());
	}
	
	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) { //td deep sql parentheses
		
		return new SqlStringBuilder(50 * expressions.size())
				.append("(")
				.appendEach(expressions, operator.sql(), e-> e.sql(table, ph))
				.append(")")
				.toString();
	}

	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		if(operator == op) {
			expressions.add(filter);
			return this;
		}
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}
