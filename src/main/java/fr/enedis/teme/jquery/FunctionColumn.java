package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.DBTable.mockTable;
import static java.util.Objects.requireNonNullElseGet;

import lombok.Getter;
import lombok.NonNull;

public final class FunctionColumn implements DBColumn {

	@Getter
	private final String mappedName;
	private final DBColumn column;
	private final DBFunction function;
	
	public FunctionColumn(@NonNull DBColumn column, @NonNull DBFunction function, String mappedName) {
		this.column = column;
		this.function = function;
		this.mappedName = requireNonNullElseGet(mappedName, ()-> function.tag(column.getMappedName()));
	}

	@Override
	public String sql(DBTable table) {
		return function.sql(column.sql(table));
	}
	
	@Override
	public String tag(DBTable table) {
		return function.tag(column.tag(table));
	}
	
	@Override
	public boolean isAggregation() {
		return function.isAggregate();
	}

	@Override
	public boolean isExpression() {
		return true;
	}
		
	@Override
	public String toString() {
		return sql(mockTable());
	}
}
