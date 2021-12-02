package fr.enedis.teme.jquery;

import static java.util.Objects.requireNonNull;

public interface DBFunction extends DBObject<String> {
	
	String getFunctionName();
		
	default boolean isAggregation() {
		return false;
	}
	
	default String getMappedName() {
		return getFunctionName().toLowerCase();
	}
	
	@Override
	default String toSql(String columnName) {
		var v = requireNonNull(columnName, ()-> getFunctionName() + " require non null column");
		return getFunctionName() + "(" + v + ")";
	}
	
	//FunctionColumn
	default FunctionColumn of(DBColumn column) {
		return new FunctionColumn(this, column);
	}

	default FunctionColumn ofAll() {
		return new FunctionColumn(this, null);
	}

}
