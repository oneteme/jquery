package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
@FunctionalInterface
public interface Adjuster<V>{
	
	V adjust(Object model, V initalValue);
	
	default V build(QueryBuilder builder, V initalValue) {
		return adjust(builder.getCurrentModel(), initalValue);
	}
}