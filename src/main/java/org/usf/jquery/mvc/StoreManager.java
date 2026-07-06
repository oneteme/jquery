package org.usf.jquery.mvc;

import static java.util.Objects.nonNull;
import static org.usf.jquery.mvc.StoreProxy.createStore;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Dialect;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StoreManager {
	
	private static final StoreManager instance = new StoreManager();
	
	private final Map<Class<?>, StoreCatalogue> stores = new LinkedHashMap<>();
	
	public static StoreManager getInstance() {
		return instance;
	}
	
	public void register(Class<? extends StoreCatalogue> clazz, DataSource ds) {
		stores.put(clazz, createStore(clazz, ds));
	}
	
	public void register(Class<? extends StoreCatalogue> clazz, DataSource ds, Dialect dialect) {
		stores.put(clazz, createStore(clazz, ds, dialect));
	}
	
	public <T extends StoreCatalogue> T getStore(Class<T> clazz){
		var res = clazz.cast(stores.get(clazz));
		if(nonNull(res)) {
			return res;
		}
		throw new NoSuchResourceException("store not found for " + clazz);
	}

	@SuppressWarnings("unchecked")
	public <T extends StoreCatalogue> T getDefaultStore(){
		if(stores.size() == 1) {
			return (T) stores.values().iterator().next();
		}
		throw new NoSuchResourceException("unable to determine default store");
	}
}
