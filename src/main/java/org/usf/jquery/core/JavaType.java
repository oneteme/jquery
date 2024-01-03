package org.usf.jquery.core;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface JavaType {
	
	Class<?> type();
	
	default String name() {
		return type().getSimpleName();
	}
	
	default boolean accept(Object o) {
		return o == null || type().isInstance(o);
	}
}