package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static java.util.Objects.requireNonNullElseGet;

import java.util.stream.Stream;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpressionColumn implements DBFilter {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final DBExpression expression;
	private final String tagname;

	public ExpressionColumn(@NonNull DBColumn column, @NonNull DBExpression expression) {
		this.column = column;
		this.expression = expression;
		this.tagname = null; //used in case expression
	}

	@Override
	public String sql(DBTable table) {
		return expression.sql(column.sql(table));
	}
	
	@Override
	public String tag(DBTable table) {
		return requireNonNullElseGet(tagname, ()-> "case_" + column.tag(table));
	}

	@Override
	public Stream<Object> args() {
		return expression.args();
	}
	
	@Override
	public String toString() {
		return sql(mockTable());
	}
	
}
