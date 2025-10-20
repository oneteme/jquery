package org.usf.jquery.web.proxy;

import static org.usf.jquery.web.proxy.SchemaInvocationHandler.schemaProxy;
import static org.usf.jquery.web.proxy.ViewInvocationHandler.buildView;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JQueryResource {

	private static final Map<Class<?>, SchemaResource> handlers = new LinkedHashMap<>();
	
	public static <T extends SchemaResource> T getSchema(Class<T> clazz) {
		return clazz.cast(handlers.computeIfAbsent(clazz, k-> schemaProxy(clazz)));
	}
	
	public static <T extends ViewResource> T getView(Class<T> clazz, Bind bind) {
		return buildView(clazz, bind, null);
	}

	public static <T extends ViewResource> T getView(Class<T> clazz, Bind bind, String schema) {
		return buildView(clazz, bind, schema);
	}
}
