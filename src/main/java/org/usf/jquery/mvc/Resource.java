package org.usf.jquery.mvc;

/**
 * 
 * @author u$f
 * 
 */
public interface Resource {
	
	<T> ResourceInvoker<T> lookup(String resource, Class<T> type);

}
