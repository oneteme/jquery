package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 * 
 */
public interface Resource {
	
	<T> MethodInvoker<T> lookup(String id, Class<T> type);

}
