package org.usf.jquery.web.proxy;

import static java.util.Collections.emptySet;
import static org.usf.jquery.web.proxy.Resource.Match.VALID;

import java.util.Set;

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
		return createContext(defaultDataset, emptySet(), emptySet(), emptySet());
	}
	
	default RequestContext createContext(String defaultDataset, Set<String> excludeViews, Set<String> excludeResources, Set<String> excludeDialects) {
		if(exposes(defaultDataset, DatasetResource.class) == VALID) {
			var dataset = invokeResource(defaultDataset, DatasetResource.class, null, null);
			return new RequestContext(this, dataset, new TypeRegistry(), excludeViews, excludeResources, excludeDialects);
		}
		throw new NoSuchResourceException("no dataset resource found for " + defaultDataset);
	}
}