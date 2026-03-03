package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.StoreProxy.createStore;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.web.NoSuchResourceException;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class Stores {
	
	private static final Map<Class<?>, StoreResource> storeMap = new LinkedHashMap<>();
	
	public static void register(Class<? extends StoreResource> clazz, DataSource ds) {
		storeMap.put(clazz, createStore(clazz, ds));
	}
	
	public static <T extends StoreResource> T getStore(Class<T> clazz){
		return clazz.cast(storeMap.get(clazz));
	}

	@SuppressWarnings("unchecked")
	public static <T extends StoreResource> T getDefaultStore(){
		if(storeMap.size() == 1) {
			return (T) storeMap.values().iterator().next();
		}
		throw new NoSuchResourceException("multiple schemas registered, specify schema class");
	}
}
