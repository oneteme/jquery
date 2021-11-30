package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import lombok.Getter;

@Getter
public final class FunctionColumn implements Column {
	
	private final Function function;
	private final Column column; // nullable

	public FunctionColumn(Function function, Column column) {
		this.function = requireNonNull(function);
		this.column = function.isColumnRequired() ? requireNonNull(column) : column;
	}

	@Override
	public String getMappedName() {
		return ofNullable(column)
				.map(Column::getMappedName)
				.orElseGet(function::getMappedName);
	}
	
	@Override
	public String getColumnName(Table table) {
		return ofNullable(column)
				.map(c-> c.getColumnName(table))
				.orElseGet(function::getMappedName);
	}
	
	@Override
	public String toSql(Table table) {
		return function.toSql(table, column);
	}
	
	@Override
	public boolean isAggregated() {
		return function.isAggregation();
	}
	
}
