package org.usf.jquery.core;

@FunctionalInterface
public interface DBObject {
	
	String sql(QueryParameterBuilder arg);

}
