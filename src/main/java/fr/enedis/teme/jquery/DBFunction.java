package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;
import static fr.enedis.teme.jquery.ValueColumn.staticColumn;

public interface DBFunction extends DBObject<String> {
	
	String getFunctionName();
	
	boolean isAggregate();
	
	@Override
	default String sql(String columnName, ParameterHolder ph) {
		return getFunctionName().toUpperCase() + "(" + requireNonBlank(columnName) + ")";
	}

	@Override
	default String tag(String columnName) {
		return getFunctionName().toLowerCase() + "_" + requireNonBlank(columnName);
	}

	
	//FunctionColumn
	default FunctionColumn of(DBColumn column, String mappedName) {
		return new FunctionColumn(column, this, mappedName);
	}
	
	default FunctionColumn of(DBColumn column) {
		return new FunctionColumn(column, this, null);
	}

	default FunctionColumn ofAll(String mappedName) {
		return new FunctionColumn(staticColumn("all", "*"), this, mappedName);
	}

	default FunctionColumn ofAll() {
		return new FunctionColumn(staticColumn("all", "*"), this, null);
	}

}
