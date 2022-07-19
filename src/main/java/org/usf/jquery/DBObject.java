package org.usf.jquery;

@FunctionalInterface
public interface DBObject {
	
	String sql(QueryParameterBuilder arg);

}
