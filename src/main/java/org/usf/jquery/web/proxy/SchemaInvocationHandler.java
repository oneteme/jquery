package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.proxy.ResourceScanner.getMethodIdentifier;
import static org.usf.jquery.web.proxy.ResourceScanner.scanBinding;
import static org.usf.jquery.web.proxy.ResourceScanner.scanExposedResources;
import static org.usf.jquery.web.proxy.ViewInvocationHandler.createViewHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.usf.jquery.core.Product;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
public final class SchemaInvocationHandler extends ResourceInvokerHandler {
	
	private final String name;
	private final Map<String, ? extends ViewResource> viewHandlers;
	private final DataSource ds;
	private final Product product;
	
	public SchemaInvocationHandler(String name, Map<String, Method> exposedMethods, Map<String, ? extends ViewResource> viewHandlers, DataSource ds, Product product) {
		super(exposedMethods);
		this.viewHandlers = viewHandlers;
		this.name = name;
		this.ds = ds;
		this.product = product;
	}
	
	@Override
	Object invokeAbstractMethod(Object proxy, Bind bind, Method method, Object[] args) { //bind method must return a resource handler
		var handler = viewHandlers.get(getMethodIdentifier(method));
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
	static <T extends Resource> T createSchemaHandler(Class<T> clazz, DataSource ds) {
		if(clazz.isInterface()) {
			var bnd = scanBinding(clazz, false);
			var map = scanExposedResources(clazz.getMethods(), (t,c)-> Resource.class.isAssignableFrom(c));
			var sub = map.entrySet()
					.stream()
					.filter(e-> isAbstract(e.getValue().getReturnType().getModifiers())) //only abstract method can be binded to sub handler
					.collect(toMap(Entry::getKey, e-> createViewHandler((Class<? extends ViewResource>)e.getValue().getReturnType(), scanBinding(e.getValue(), true))));
			return clazz.cast(newProxyInstance(SchemaInvocationHandler.class.getClassLoader(), new Class<?>[]{clazz}, 
					new SchemaInvocationHandler(nonNull(bnd) ? bnd.value() : null, map, sub, ds, null)));
		}
		throw new JQueryConfigurationException("schema must be an interface : " + clazz);
	}
}
