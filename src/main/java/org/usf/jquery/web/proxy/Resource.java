package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 * 
 */
public interface Resource {
	
	Match exposes(String id, Class<?> type); 
	
	<T> T invokeResource(String id, Class<T> type, Entry[] args, RequestContext ctx);
	
	enum Match {
		
		/** resource not found **/
        NONE, 
        /** resource found but type mismatch **/
        TYPE, 
        /** resource found but explicitly hidden **/
        HIDDEN, 
        /** resource found and valid **/
        VALID   
    }
}
