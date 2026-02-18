package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.usf.jquery.web.proxy.ResourceIntrospector.resolveIdentifier;
import static org.usf.jquery.web.proxy.ResourceIntrospector.scanBind;
import static org.usf.jquery.web.proxy.ResourceIntrospector.discoverExposedMethods;
import static org.usf.jquery.web.proxy.ViewProxy.createView;

import java.lang.reflect.Method;
import java.util.Map;

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
public final class SchemaProxy extends ResourceProxy {
	
	private final String name;
	private final Map<String, ? extends ViewResource> views;
	private final DataSource ds;
	private final Product product;
	
	public SchemaProxy(String name, Map<String, Method> exposedMethods, Map<String, ? extends ViewResource> views, DataSource ds, Product product) {
		super(exposedMethods);
		this.views = views;
		this.name = name;
		this.ds = ds;
		this.product = product;
	}
	
	@Override
	Object invokeAbstractMethod(Object proxy, Bind bind, Method method, Object[] args) { //bind method must return a resource handler
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
	static <T extends Resource> T createSchema(Class<T> clazz, DataSource ds) {
		if(clazz.isInterface()) {
			var bnd = clazz.isAnnotationPresent(Bind.class) ? scanBind(clazz).value() : null;
			var map = discoverExposedMethods(clazz, (t,c)-> ViewResource.class.isAssignableFrom(c));
			var sub = stream(clazz.getDeclaredMethods())
					.filter(m-> isAbstract(m.getModifiers())) //only abstract method can be binded to sub handler
					.collect(toMap(ResourceIntrospector::resolveIdentifier, 
							m-> createView((Class<? extends ViewResource>)m.getReturnType(), m.getAnnotation(Bind.class))));
			return clazz.cast(newProxyInstance(SchemaProxy.class.getClassLoader(), new Class<?>[]{clazz}, 
					new SchemaProxy(bnd, map, sub, ds, null)));
		}
		throw new ResourceMappingException("schema must be an interface : " + clazz);
	}
}
