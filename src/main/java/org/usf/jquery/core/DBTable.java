package org.usf.jquery.core;

public interface DBTable extends DBObject {

	String reference(); //JSON & TAG
	
	@Override
	default String sql(QueryParameterBuilder builder, Object[] args) {//schema, suffix ?
		return sql();
	}
	
	String sql();
	
}
