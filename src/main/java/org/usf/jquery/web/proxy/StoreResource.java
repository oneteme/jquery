package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.Resource.Match.VALID;

import org.usf.jquery.core.Store;
import org.usf.jquery.web.NoSuchResourceException;

/**
 * 
 * @author u$f
 *
 */
public interface StoreResource extends Store, Resource {
	
	//can override createContext to provide a custom TypeRegistry 
	default RequestContext createContext(String defaultView) {
		if(exposes(defaultView, DatasetResource.class) == VALID) {
			return new RequestContext(this, invokeResource(defaultView, DatasetResource.class, null, null));
		}
		throw new NoSuchResourceException("");
	}
}