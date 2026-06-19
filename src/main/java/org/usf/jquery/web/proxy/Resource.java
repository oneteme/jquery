package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 * 
 */
public interface Resource {
	
	<T> ResourceInvoker<T> lookup(String resource, Class<T> type);

}
