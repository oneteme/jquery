package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Utils.mapNullableOrNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import lombok.Getter;

@Getter
public final class FunctionColumn implements DBColumn {
	
	private final DBFunction function;
	private final DBColumn column; // nullable

	public FunctionColumn(DBFunction function, DBColumn column) {
		this.function = requireNonNull(function);
		this.column = column;
	}

	@Override
	public String getMappedName() {
		return ofNullable(column)
				.map(DBColumn::getMappedName)
				.orElseGet(function::getMappedName);
	}
	
	@Override
	public String toSql(DBTable table) {
		return function.toSql(mapNullableOrNull(column, c-> c.toSql(table)));
	}
	
	@Override
	public boolean isAggregated() {
		return function.isAggregation();
	}
	
	@Override
	public String toString() {
		
		return function.toSql(mapNullableOrNull(column, DBColumn::getMappedName));
	}
	
}
