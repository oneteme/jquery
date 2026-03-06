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
			var defaultDataset = invokeResource(defaultView, DatasetResource.class, null, null);
			return new RequestContext(this, defaultDataset, new TypeRegistry());
		}
		throw new NoSuchResourceException("no default dataset found for " + defaultView);
	}
}