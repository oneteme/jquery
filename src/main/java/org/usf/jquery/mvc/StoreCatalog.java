package org.usf.jquery.mvc;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.mvc.MethodUtils.lookupAccessibleMethod;
import static org.usf.jquery.mvc.ResourceInvoker.ofMethod;

import org.usf.jquery.core.Store;

/**
 * 
 * @author u$f
 *
 */
public interface StoreCatalog extends Store, Catalog {

	default RequestContext createContext(String defaultDataset) {
		var v = lookup(defaultDataset, DatasetCatalog.class);
		if(nonNull(v) && v.isAccessible()) {
			return new RequestContext(this, v.invoke(), typeRegistry());
		}
		throw new IllegalAccessError("Dataset " + defaultDataset + " is not accessible or does not exist");
	}
	
	default TypeRegistry typeRegistry() {
		return new TypeRegistry();
	}
	
	default ViewRegistry viewRegistry() {
		return new ViewRegistry();
	}
	
	default <T> ResourceInvoker<T> lookup(Catalog sub, String resource, Class<T> type) {
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
				? lookupAccessibleMethod(resource, dialect.getClass(), type, composer.getClass()) //require Composer instance
				: lookupAccessibleMethod(resource, dialect.getClass(), type); //no parameter
		return nonNull(mth) ? ofMethod(true, mth, dialect) : null;
	}
}