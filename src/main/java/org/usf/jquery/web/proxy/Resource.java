package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 * 
 */
public interface Resource {
	
	boolean exposes(String id, Class<?> type);
	
	<T> T invokeResource(String id, Class<T> type, Entry[] args, RequestContext ctx);
}
