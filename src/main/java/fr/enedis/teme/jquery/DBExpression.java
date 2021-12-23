package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.ConstantColumn.staticColumn;
import static fr.enedis.teme.jquery.Validation.requireNonBlank;

public interface DBExpression extends DBObject<String> {
	
	String getFunctionName();
		
	default boolean isAggregation() {
		return false;
	}
	
	default String mappedName(String columnName) {
		return getFunctionName().toLowerCase() + "_" + requireNonBlank(columnName);
	}
	
	@Override
	default String toSql(String columnName) {
		return getFunctionName() + "(" + requireNonBlank(columnName) + ")";
	}
	
	//FunctionColumn
	default FunctionColumn of(DBColumn column, String mappedName) {
		return new FunctionColumn(this, column, mappedName);
	}
	
	default FunctionColumn of(DBColumn column) {
		return new FunctionColumn(this, column, null);
	}

	default FunctionColumn ofAll(String mappedName) {
		return new FunctionColumn(this, staticColumn("all", "*"), mappedName);
	}

	default FunctionColumn ofAll() {
		return new FunctionColumn(this, staticColumn("all", "*"), null);
	}

}
