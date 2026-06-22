package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.web.proxy.StoreProxy.createStore;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Dialect;
import org.usf.jquery.web.NoSuchResourceException;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StoreManager {
	
	private static final StoreManager instance = new StoreManager();
	
	private final Map<Class<?>, StoreResource> stores = new LinkedHashMap<>();
	
	public static StoreManager getInstance() {
		return instance;
	}
	
	public void register(Class<? extends StoreResource> clazz, DataSource ds) {
		stores.put(clazz, createStore(clazz, ds));
	}
	
	public void register(Class<? extends StoreResource> clazz, DataSource ds, Dialect dialect) {
		stores.put(clazz, createStore(clazz, ds, dialect));
	}
	
	public <T extends StoreResource> T getStore(Class<T> clazz){
		var res = clazz.cast(stores.get(clazz));
		if(nonNull(res)) {
			return res;
		}
		throw new NoSuchResourceException("store not found for " + clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends StoreResource> T getDefaultStore(){
		if(stores.size() == 1) {
			return (T) stores.values().iterator().next();
		}
		throw new NoSuchResourceException("unable to determine default store");
	}
}
