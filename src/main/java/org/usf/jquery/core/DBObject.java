package org.usf.jquery.core;

@FunctionalInterface
public interface DBObject {
	
	String sql(QueryParameterBuilder builder, Object[] args);

}
