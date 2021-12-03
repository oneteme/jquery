package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;

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
		return getFunctionName() + "(" + requireNonBlank(columnName) + ")";
	}
	
	//FunctionColumn
	default FunctionColumn of(DBColumn column) {
		return new FunctionColumn(this, column);
	}

	default FunctionColumn ofAll() {
		return new FunctionColumn(this, null);
	}

}
