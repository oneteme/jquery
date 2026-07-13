package org.usf.jquery.mvc;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.hash;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.mvc.DatabaseIntrospector.storeDialect;
import static org.usf.jquery.mvc.DatasetProxy.createDataset;
import static org.usf.jquery.mvc.MethodUtils.getMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Dialect;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public final class StoreProxy extends ResourceProxy {
	
	private final String name;
	
	StoreProxy(String name, Map<String, Method> exposedMethods, Map<Method, Object> resourcesCache) {
		super(exposedMethods, resourcesCache);
		this.name = name;
	}
	
	@Override
	int invokeHashCode(Object proxy, Object[] args) {
		return hash(name);
	}
	
	@Override
	String invokeToString(Object proxy, Object[] args) {
		return name;
	}
	
	@Override
	public String toString() {
		return "StoreProxy {"+name+"}";
	}

	static <T extends StoreCatalog> T createStore(Class<T> clazz, DataSource ds) {
		return createStore(clazz, ds, storeDialect(ds));
	}
	
	@SuppressWarnings("unchecked")
	static <T extends StoreCatalog> T createStore(Class<T> type, DataSource ds, Dialect dialect) {
		if(type.isInterface()) {
			var name = type.isAnnotationPresent(Bind.class) ? scanBind(type).value() : null;
			var map = discoverExposedMethods(type, StoreCatalog.class, (m,b)-> DatasetCatalog.class.isAssignableFrom(m.getReturnType()));
			var cache = new HashMap<Method, Object>();
			var proxy = new StoreProxy(name, map, cache);
			var store = type.cast(newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, proxy));
			cache.put(getMethod("name", type), name);
			cache.put(getMethod("dataSource", type), ds);
			cache.put(getMethod("dialect", type), dialect);
			cache.putAll(map.values().stream()
					.filter(m-> isAbstract(m.getModifiers()) && m.getParameterCount() == 0) //only abstract (not parameterized) method can be binded to sub handler
					.parallel()
					.collect(toMap(identity(), m-> createDataset((Class<? extends DatasetCatalog<T>>) m.getReturnType(), m.getAnnotation(Bind.class), store, ds))));
			return store;
		}
		throw new ResourceMappingException("schema must be an interface : " + type);
	}
}
