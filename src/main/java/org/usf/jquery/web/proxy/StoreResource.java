package org.usf.jquery.web.proxy;

import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.ClassUtils.lookupAccessibleMethod;
import static org.usf.jquery.web.proxy.ResourceInvoker.ofMethod;
import static org.usf.jquery.web.proxy.RestrictedStore.restrict;

import java.util.Set;

import org.usf.jquery.core.Store;

/**
 * 
 * @author u$f
 *
 */
public interface StoreResource extends Store, Resource {
	
	//can override createContext to provide a custom TypeRegistry 
	
	default RequestContext createContext(String defaultDataset) {
		return createContext(defaultDataset, emptySet(), emptySet());
	}
	
	default RequestContext createContext(String defaultDataset, Set<String> excludeResources, Set<String> excludeDialects) {
		var v = lookup(defaultDataset, DatasetResource.class);
		if(nonNull(v) && v.isAccessible()) {
			var store = restrict(this, excludeResources, excludeDialects);
			return new RequestContext(store, v.invoke(), new TypeRegistry());
		}
		throw new IllegalAccessError("Dataset " + defaultDataset + " is not accessible or does not exist");
	}

	default <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type) {
		return lookupDialect(resource, type, null);
	}
	
	default <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type, Object composer) {
		if(isNull(composer)) { //cannot override composers in stores
			var res = lookup(resource, type);
			if(nonNull(res)) {
				if(res.isAccessible()) {
					return res;
				}
				throw new ResourceAccessException(resource + " of type " + type.getSimpleName() + " is not accessible in store " + this);
			}
		}
		var dialect = dialect();
		var mth = nonNull(composer) 
				? lookupAccessibleMethod(resource, dialect.getClass(), composer.getClass())
				: lookupAccessibleMethod(resource, dialect.getClass()); //no parameter
		if(nonNull(mth)) {
			var npr = nonNull(composer) ? 1 : 0;
			if(type.isAssignableFrom(mth.getReturnType()) && mth.getParameterCount() == npr) {
				return ofMethod(true, mth, dialect);
			}
		}
		return null;
	}
}