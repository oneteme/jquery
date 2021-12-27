package fr.enedis.teme.jquery;

import static fr.enedis.teme.jquery.Validation.requireNonBlank;

import lombok.NonNull;

public interface DBFunction extends DBObject<String> {
	
	String getFunctionName();
	
	boolean isAggregate();
	
	@Override
	default String sql(String columnName, ParameterHolder ph) {
		return getFunctionName().toUpperCase() + "(" + requireNonBlank(columnName) + ")";
	}
	
	//FunctionColumn
	default FunctionColumn of(@NonNull DBColumn column) {
		return new FunctionColumn(column, this);
	}
	
}
