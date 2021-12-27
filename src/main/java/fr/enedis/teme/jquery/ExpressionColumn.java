package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.staticSql;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ExpressionColumn implements DBFilter {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final DBExpression expression;

	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return expression.sql(column.sql(table, ph), ph);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), staticSql());
	}
	
}
