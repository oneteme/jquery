package org.usf.jquery.core;

import static java.util.Objects.isNull;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface JavaType {
	
	Class<?> getCorrespondingClass();
	
	default boolean accept(Object o) {
		return isNull(o) || getCorrespondingClass().isInstance(o);
	}
	
	public interface Typed {
		
		JDBCType getType();  // return null by default
	}
}