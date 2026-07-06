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
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Dialect;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
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
		return "StoreProx {"+name+"}";
	}

	static <T extends StoreCatalogue> T createStore(Class<T> clazz, DataSource ds) {
		return createStore(clazz, ds, storeDialect(ds));
	}
	
	@SuppressWarnings("unchecked")
	static <T extends StoreCatalogue> T createStore(Class<T> clazz, DataSource ds, Dialect dialect) {
		if(clazz.isInterface()) {
			var name = clazz.isAnnotationPresent(Bind.class) ? scanBind(clazz).value() : null;
			var map = discoverExposedMethods(clazz, StoreCatalogue.class, (m,b)-> DatasetCatalogue.class.isAssignableFrom(m.getReturnType()));
			Map<Method, Object> cache = map.values().stream()
					.filter(m-> isAbstract(m.getModifiers())) //only abstract method can be binded to sub handler
					.parallel().collect(toMap(identity(), m-> createDataset((Class<? extends DatasetCatalogue>) m.getReturnType(), m.getAnnotation(Bind.class), name, ds)));
			cache.put(getMethod("name", clazz), name);
			cache.put(getMethod("dataSource", clazz), ds);
			cache.put(getMethod("dialect", clazz), dialect);
			var store = new StoreProxy(name, map, cache);
			return clazz.cast(newProxyInstance(StoreProxy.class.getClassLoader(), new Class<?>[]{clazz}, store));
		}
		throw new ResourceMappingException("schema must be an interface : " + clazz);
	}
}
