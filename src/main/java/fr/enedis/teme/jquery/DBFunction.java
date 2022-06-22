package fr.enedis.teme.jquery;

import lombok.NonNull;

public interface DBFunction extends DBObject<String> {
	
	String physicalName();
	
	boolean isAggregate();
	
	@Override
	default String sql(String cn, QueryParameterBuilder ph) {
		return physicalName() + "(" + cn + ")";
	}
	
	default FunctionColumn of(@NonNull DBColumn column) {
		return new FunctionColumn(column, this);
	}
	
	static DBFunction definedFunction(final String name) {
		return new DBFunction() {
			
			@Override
			public String physicalName() {
				return name;
			}
			
			@Override
			public boolean isAggregate() {
				return false;
			}
		};
	}
	
}
