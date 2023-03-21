package org.usf.jquery.core;

public interface DBTable extends DBObject {
	
	String reference(); //SQL
	
	String columnName(TaggableColumn desc);
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {
		throw new UnsupportedOperationException();
	}
	
}
