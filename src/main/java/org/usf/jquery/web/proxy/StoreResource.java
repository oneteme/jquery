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
	default RequestContext createContext(String defaultDataset) {
		if(exposes(defaultDataset, DatasetResource.class) == VALID) {
			var dataset = invokeResource(defaultDataset, DatasetResource.class, null, null);
			return new RequestContext(this, dataset, new TypeRegistry());
		}
		throw new NoSuchResourceException("no dataset resource found for " + defaultDataset);
	}
}