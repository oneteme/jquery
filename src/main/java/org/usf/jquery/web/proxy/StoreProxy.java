package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.proxy.DatabaseIntrospector.storeMetadata;
import static org.usf.jquery.web.proxy.DatasetProxy.createDataset;
import static org.usf.jquery.web.proxy.ResourceIntrospector.discoverExposedMethods;
import static org.usf.jquery.web.proxy.ResourceIntrospector.resolveIdentifier;
import static org.usf.jquery.web.proxy.ResourceIntrospector.scanBind;

import java.lang.reflect.Method;
import java.util.Map;

import javax.sql.DataSource;

import org.usf.jquery.core.Comparators;
import org.usf.jquery.core.Operators;
import org.usf.jquery.core.StoreMetadata;

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
	private final Map<String, ? extends DatasetResource> views;
	private final StoreMetadata metadata;
	
	StoreProxy(String name, Map<String, Method> exposedMethods, Map<String, ? extends DatasetResource> views, StoreMetadata metadata) {
		super(exposedMethods);
		this.views = views;
		this.name = name;
		this.metadata = metadata;
	}
	
	@Override
	Object invokeAbstractMethod(Object proxy, Method method, Object[] args) { //bind method must return a resource handler
		if(method.getReturnType() == StoreMetadata.class && method.getParameterCount() == 0 && "metadata".equals(method.getName())) {
			return metadata;
		}
		if(Operators.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0 && "operators".equals(method.getName())) {
			return metadata.comparators();
		}
		if(Comparators.class.isAssignableFrom(method.getReturnType()) && method.getParameterCount() == 0 && "comparators".equals(method.getName())) {
			return metadata.comparators();
		}
		var handler = views.get(resolveIdentifier(method));
		if(nonNull(handler)) {
			return handler;	
		}
		throw new IllegalStateException("no resource handler found for " + method);
	}

	@Override
	int invokeHashCode(Object proxy, Object[] args) {
		return hash(name);
	}
	
	@Override
	String invokeToString(Object proxy, Object[] args) {
		return name;
	}

	@SuppressWarnings("unchecked")
	static <T extends StoreResource> T createStore(Class<T> clazz, DataSource ds) {
		if(clazz.isInterface()) {
			var bnd = clazz.isAnnotationPresent(Bind.class) ? scanBind(clazz).value() : null;
			var map = discoverExposedMethods(clazz, (t,c)-> DatasetResource.class.isAssignableFrom(c));
			var sub = stream(clazz.getDeclaredMethods())
					.filter(m-> isAbstract(m.getModifiers())) //only abstract method can be binded to sub handler
					.parallel().collect(toMap(ResourceIntrospector::resolveIdentifier, 
							m-> createDataset((Class<? extends DatasetResource>) m.getReturnType(), m.getAnnotation(Bind.class), bnd, ds)));
			return clazz.cast(newProxyInstance(StoreProxy.class.getClassLoader(), new Class<?>[]{clazz}, 
					new StoreProxy(bnd, map, sub, storeMetadata(ds))));
		}
		throw new ResourceMappingException("schema must be an interface : " + clazz);
	}
}
