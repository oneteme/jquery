package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.LogicalOperator.*;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class ColumnFilter implements DBFilter {

	@NonNull
	private final DBColumn column;
	@NonNull
	private final CompareOperator operator;
	private final Object value; //nullable

	@Override
	public String sql(DBTable table, ParameterHolder ph) {
		return column.sql(table, ph) + operator.sql(value, ph);
	}

	@Override
	public DBFilter and(DBFilter filter) {
		return new ColumnFilterGroup(AND, this, filter);
	}

	@Override
	public DBFilter or(DBFilter filter) {
		return new ColumnFilterGroup(OR, this, filter);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}
}
