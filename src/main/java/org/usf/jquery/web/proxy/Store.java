package org.usf.jquery.web.proxy;

/**
 * 
 * @author u$f
 *
 */
public interface Store extends Resource {
	
	//can override defaultView to provide a custom TypeRegistry 
	default RequestContext createContext(String defaultView) {
		return new RequestContext(this, invokeResource(defaultView, ViewResource.class, null, null));
	}
}