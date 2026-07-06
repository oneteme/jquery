package org.usf.jquery.mvc;

/**
 * 
 * @author u$f
 * 
 */
public interface Catalogue {
	
	<T> ResourceInvoker<T> lookup(String resource, Class<T> type);

}
