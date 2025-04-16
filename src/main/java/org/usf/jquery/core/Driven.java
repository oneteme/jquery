package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Driven<T, V> {
	
	T adjuster(Adjuster<V> adjuster);
	
	@FunctionalInterface
	interface Adjuster<V>{
		
		V adjust(V value, Object model);		
	}
}
