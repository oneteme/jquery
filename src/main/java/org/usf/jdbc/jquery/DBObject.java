package org.usf.jdbc.jquery;

@FunctionalInterface
public interface DBObject {
	
	String sql(QueryParameterBuilder arg);

}
