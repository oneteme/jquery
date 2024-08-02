package org.usf.jquery.web;

/**
 * 
 * @author u$f
 *
 */
public interface IterableViewDecorator<T> extends ViewDecorator {

	void tableName(String name, T ctx);
	
}
