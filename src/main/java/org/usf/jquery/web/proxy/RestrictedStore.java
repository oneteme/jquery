package org.usf.jquery.web.proxy;

import static java.util.Objects.requireNonNullElseGet;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.Collections;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor
public class RestrictedStore implements StoreResource {

	@Delegate
	private final StoreResource store;
	private final Set<String> excludeResources;
	private final Set<String> excludeDialects;
	
	@Override
	public <T> ResourceInvoker<T> lookup(String resource, Class<T> type) {
		if(!excludeResources.contains(resource)) {
			return store.lookup(resource, type);
		}
		throw new ResourceAccessException("Resource " + resource + " is not allowed");
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type) {
		return lookupDialect(resource, type);
	}
	
	@Override
	public <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type, Object composer) {
		if(!excludeDialects.contains(resource)) {
			return store.lookupDialect(resource, type, composer);
		}
		throw new ResourceAccessException("Resource " + resource + " is not allowed");
	}
	
	public static StoreResource restrict(StoreResource store, Set<String> excludeResources, Set<String> excludeDialects) {
		if(isEmpty(excludeResources) && isEmpty(excludeDialects)) {
			return store;
		}
		return new RestrictedStore(store, 
				requireNonNullElseGet(excludeResources, Collections::emptySet), 
				requireNonNullElseGet(excludeDialects, Collections::emptySet));
	}

}
