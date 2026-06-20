package org.usf.jquery.web.proxy;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.ClassUtils.lookupAccessibleMethod;
import static org.usf.jquery.web.proxy.ResourceInvoker.ofMethod;

import org.usf.jquery.core.Store;

/**
 * 
 * @author u$f
 *
 */
public interface StoreResource extends Store, Resource {
	
	//can override createContext to provide a custom TypeRegistry 
	
	default RequestContext createContext(String defaultDataset) {
		var v = lookup(defaultDataset, DatasetResource.class);
		if(nonNull(v) && v.isAccessible()) {
			return new RequestContext(this, v.invoke(), new TypeRegistry());
		}
		throw new IllegalAccessError("Dataset " + defaultDataset + " is not accessible or does not exist");
	}

	default <T> ResourceInvoker<T> lookup(Resource sub, String resource, Class<T> type) {
		return nonNull(sub) ? sub.lookup(resource, type) : null;
	}
	
	default <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type) {
		return lookupDialect(resource, type, null);
	}
	
	default <T> ResourceInvoker<T> lookupDialect(String resource, Class<T> type, Object composer) {
		if(isNull(composer)) { //cannot override composers in stores
			var res = lookup(resource, type);
			if(nonNull(res)) {
				return res;
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