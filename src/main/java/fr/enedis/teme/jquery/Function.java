package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

public interface Function {
	
	String getMappedName();
	
	String getColumnName();
		
	boolean isAggregation();

	default boolean requiredColumn() {
		return true;
	}
	
	default String toSql(Table table, Column column) {
		return getColumnName() + "(" + requireNonNull(column).toSql(table) + ")";
	}
	
	default FunctionColumn of(Column column) {
		return new FunctionColumn(this, column);
	}

	default FunctionColumn ofAll() {
		return new FunctionColumn(this, null);
	}

}
