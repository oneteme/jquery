package org.usf.jquery.mvc;

/**
 * 
 * @author u$f
 * 
 */
@FunctionalInterface
public interface Catalog {
	
	<T> ResourceInvoker<T> lookup(String resource, Class<T> type);

}
