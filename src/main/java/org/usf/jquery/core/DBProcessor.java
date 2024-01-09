package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface DBProcessor<T> extends DBObject {
	
	T args(Object... args);

}
