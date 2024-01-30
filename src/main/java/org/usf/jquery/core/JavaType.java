package org.usf.jquery.core;

import static java.util.Objects.isNull;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface JavaType {
	
	Class<?> typeClass();
	
	default boolean accept(Object o) {
		return isNull(o) || typeClass().isInstance(o);
	}
	
	public interface Typed {
		
		JavaType getType();  // return null by default
	}
}