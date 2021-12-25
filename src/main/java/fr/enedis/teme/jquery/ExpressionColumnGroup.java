package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.LogicalOperator.AND;
import static fr.enedis.teme.jquery.LogicalOperator.OR;
import static fr.enedis.teme.jquery.DBTable.mockTable;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpressionColumnGroup implements DBFilter {
	
	private final LogicalOperator operator;
	private final ExpressionColumn[] expression;
	private final String tagname; //nullable

	@Override
	public String sql(DBTable obj) {
		return Stream.of(expression)
				.map(e-> e.sql(obj))
				.collect(joining(operator.toString()));
	}
	
	@Override
	public String tag(DBTable table) {
		return tagname;
	}

	@Override
	public Stream<Object> args() {
		return Stream.of(expression).flatMap(ExpressionColumn::args);
	}
	
	@Override
	public String toString() {
		return sql(mockTable());
	}

	public static ExpressionColumnGroup and(@NonNull ExpressionColumn... expressions) {
		return and(null, expressions);
	}
	
	public static ExpressionColumnGroup and(String mappedName, @NonNull ExpressionColumn... expressions) {
		return new ExpressionColumnGroup(AND, expressions, mappedName);
	}

	public static ExpressionColumnGroup or(@NonNull ExpressionColumn... expressions) {
		
		return or(null, expressions);
	}
	public static ExpressionColumnGroup or(String mappedName, @NonNull ExpressionColumn... expressions) {
		
		return new ExpressionColumnGroup(OR, expressions, mappedName);
	}

}
