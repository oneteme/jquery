package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Invocable<T> {

	T invoke(JavaType type, Object... args);
}
