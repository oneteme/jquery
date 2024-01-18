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
	
	default boolean accept(Object o) { // nullable by default
		return isNull(o) || type().isInstance(o);
	}
}