package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.stream.Stream;

import lombok.NonNull;

public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final Collection<DBFilter> expression;

	public ColumnFilterGroup(@NonNull LogicalOperator operator, @NonNull DBFilter... expression) {//assert length > 1
		this.operator = operator;
		this.expression = Stream.of(expression).collect(toList());
	}
	
	@Override
	public String sql(DBTable obj, ParameterHolder ph) { //td deep sql parentheses
		
		return expression.stream()
				.map(e-> e instanceof ColumnFilterGroup 
						? "(" + e.sql(obj, ph)+")" 
						: e.sql(obj, ph))
				.collect(joining(operator.toString()));
	}

	@Override
	public DBFilter append(LogicalOperator op, DBFilter filter) {
		if(operator == op) {
			expression.add(filter);
			return this;
		}
		return new ColumnFilterGroup(op, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
	
}
