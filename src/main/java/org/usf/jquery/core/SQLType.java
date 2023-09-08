package org.usf.jquery.core;

/**
 * 
 * @author u$f
 * 
 */
public interface SQLType {

	int getValue();

	Class<?> getJavaType();
	
	default boolean isAutoType() {
		return false;
	}
	
}