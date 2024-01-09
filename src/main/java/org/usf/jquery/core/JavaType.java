package org.usf.jquery.core;

import static java.util.Objects.isNull;

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
		return isNull(o) || type().isInstance(o);
	}
}