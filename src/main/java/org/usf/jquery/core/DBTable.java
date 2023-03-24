package org.usf.jquery.core;

@FunctionalInterface
public interface DBTable extends DBObject {
	
	String reference(); //SQL
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		return reference();
	}
	
}
