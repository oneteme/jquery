package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public interface Store {
	
	StoreMetadata metadata();
		
	Operators operators();
		
	Comparators comparators();
	
	default Query build(QueryComposer query) {
		return query.compose().buildQuery(null, false);
	}
}
