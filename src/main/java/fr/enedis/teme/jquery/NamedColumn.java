package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static fr.enedis.teme.jquery.ParameterHolder.addWithValue;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NamedColumn implements DBColumn {

	@NonNull
	private final String name;
	@NonNull
	private final DBColumn column;

	@Override
	public String sql(DBTable table, ParameterHolder arg) {
		return column.sql(table, arg);
	}

	@Override
	public String getTag() {
		return name;
	}

	@Override
	public boolean isExpression() {
		return column.isExpression();
	}

	@Override
	public boolean isAggregation() {
		return column.isAggregation();
	}

	@Override
	public boolean isConstant() {
		return column.isConstant();
	}

	@Override
	public NamedColumn as(String name) { // map
		return new NamedColumn(name, this.column);
	}
	
	@Override
	public String toString() {
		return sql(mockTable(), addWithValue());
	}

}
