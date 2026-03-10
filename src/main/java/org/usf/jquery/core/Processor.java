package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Processor<T> extends DBObject, Invocable<T> {
	
	T invoke(JavaType type, Object... args);
}
