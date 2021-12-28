package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ColumnFilterGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final DBFilter[] expression;

	@Override
	public String sql(DBTable obj, ParameterHolder ph) {
		return Stream.of(expression)
				.map(e-> e.sql(obj, ph))
				.collect(joining(operator.toString()));
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}

	public static ColumnFilterGroup and(@NonNull ColumnFilter... expressions) {
		return new ColumnFilterGroup(AND, requireNonEmpty(expressions));
	}

	public static ColumnFilterGroup or(@NonNull ColumnFilter... expressions) {
		
		return new ColumnFilterGroup(OR, requireNonEmpty(expressions));
	}

}
