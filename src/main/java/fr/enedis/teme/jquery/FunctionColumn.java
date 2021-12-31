package fr.enedis.teme.jquery;


import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;
import static fr.enedis.teme.jquery.Taggable.prefix;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class FunctionColumn implements DBColumn {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final DBFunction function;

	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return function.sql(column.sql(table, ph), ph);
	}
	
	@Override
	public String getTag() {
		return prefix(function.getFunctionName().toLowerCase(), column);
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
