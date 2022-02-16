package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.QueryParameterBuilder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FunctionColumn implements DBColumn {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final DBFunction function;

	@Override
	public String sql(DBTable table, QueryParameterBuilder ph) {
		return function.sql(column.sql(table, ph), ph);
	}
	
	@Override
	public boolean isExpression() {
		return true;
	}

	@Override
	public boolean isAggregation() {
		return function.isAggregate();
	}

	@Override
	public boolean isConstant() {
		return false;
	}
		
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
}
