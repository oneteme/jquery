package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface DBObject {
	
	String sql(QueryVariables builder, Object[] args);

}
