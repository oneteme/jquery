package org.usf.jquery.web.proxy;

import org.usf.jquery.web.NoSuchResourceException;

/**
 * 
 * @author u$f
 *
 */
public interface StoreResource extends Resource {
	
	//can override createContext to provide a custom TypeRegistry 
	default RequestContext createContext(String defaultView) {
		if(exposes(defaultView, DatasetResource.class)) {
			return new RequestContext(this, invokeResource(defaultView, DatasetResource.class, null, null));
		}
		throw new NoSuchResourceException("");
	}
}