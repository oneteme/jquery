package fr.enedis.teme.jquery;

import lombok.NonNull;

public interface DBFunction extends DBObject<String> {
	
	String physicalName();
	
	boolean isAggregate();
	
	@Override
	default String sql(String cn, ParameterHolder ph) {
		return physicalName() + "(" + cn + ")";
	}
	
	default FunctionColumn of(@NonNull DBColumn column) {
		return new FunctionColumn(column, this);
	}
	
}
