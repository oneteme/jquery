package org.usf.jquery.web;

import org.usf.jquery.core.DBView;

/**
 * 
 * @author u$f
 *
 */
public interface IterableViewDecorator<T> extends ViewDecorator {

	void tableName(String name, T ctx);
	
}
