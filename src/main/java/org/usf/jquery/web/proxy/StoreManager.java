package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Stores.getCurrentStore;
import static org.usf.jquery.core.Stores.setCurrentStore;
import static org.usf.jquery.web.proxy.StoreProxy.createStore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.sql.DataSource;

import org.usf.jquery.core.QueryComposer;
import org.usf.jquery.core.QueryExecutor2;
import org.usf.jquery.core.ResultSetMapper;
import org.usf.jquery.web.NoSuchResourceException;

import lombok.NoArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class StoreManager implements QueryExecutor2 {
	
	private static final StoreManager instance = new StoreManager();
	
	private final Map<Class<?>, StoreResource> stores = new LinkedHashMap<>();
	
	public static StoreManager getInstance() {
		return instance;
	}
	
	public void register(Class<? extends StoreResource> clazz, DataSource ds) {
		stores.put(clazz, createStore(clazz, ds));
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
	
	public <S extends StoreResource, T> T execute(Class<S> clazz, Function<S, QueryComposer> fn, ResultSetMapper<T> mapper) {
		return withStore(clazz, s-> execute(clazz, fn.apply(s), mapper));
	}
	
	public <S extends StoreResource, T> T execute(Class<S> clazz, QueryComposer query, ResultSetMapper<T> mapper) {
		var store = getStore(clazz);
		var exec = store instanceof QueryExecutor2 qe ? qe : this; //allow stores to override default execution strategy
		return exec.execute(query.compose().build(), mapper, store.metadata().dataSource());
	}
	
	public <S extends StoreResource, T> T withStore(Class<S> clazz, Function<S,T> fn) {
		var prv = getCurrentStore();
		var str = getStore(clazz);
		setCurrentStore(str);
		try {
			return fn.apply(str);
		}
		finally {
			setCurrentStore(prv);
		}
	}
}
