package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;
import static fr.enedis.teme.jquery.Validation.requireNonEmpty;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpressionColumnGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final ExpressionColumn[] expression;

	@Override
	public String sql(DBTable obj, ParameterHolder ph) {
		return Stream.of(expression)
				.map(e-> e.sql(obj, ph))
				.collect(joining(operator.toString()));
	}
	

	@Override
	public String toString() {
		return sql(mockTable(), staticSql());
	}

	public static ExpressionColumnGroup and(@NonNull ExpressionColumn... expressions) {
		return new ExpressionColumnGroup(AND, requireNonEmpty(expressions));
	}

	public static ExpressionColumnGroup or(@NonNull ExpressionColumn... expressions) {
		
		return new ExpressionColumnGroup(OR, requireNonEmpty(expressions));
	}

}
