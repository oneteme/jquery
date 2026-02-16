package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DBView.view;
import static org.usf.jquery.web.proxy.ResourceScanner.scanBinding;
import static org.usf.jquery.web.proxy.ResourceScanner.scanResources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.usf.jquery.core.Product;
import org.usf.jquery.web.spec.SchemaResource;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaInvocationHandler implements InvocationHandler {
	
	private final Class<?> schemaType;
	private final String name;
	private final Product product;
	private final DataSource ds;
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			return InvocationHandler.invokeDefault(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			var bind = method.getAnnotation(Bind.class); //required
			if(nonNull(bind) && !bind.value().isEmpty()) {
				return buildView(method, bind, args);
			}
			throw new IllegalArgumentException("missing decorator @Bind on " + method);
		}
		return switch (method.getName()) {
		case "equals"-> proxy == args[0];
		case "hashCode"-> hash(name);
		case "toString"-> name;
		default -> throw new IllegalStateException("unexpected method invocation " + method);
		};
	}

	Object buildView(Method method, Bind bind, Object[] args) {
		var type = method.getReturnType();
		var view = switch(bind.type()) {
		case REF-> view(bind.value(), name);
		//case REQ-> evalView(parseEntry(bind.value()), null)
		default -> throw new UnsupportedOperationException("not implemented");
		};
		return (newProxyInstance(ViewInvocationHandler.class.getClassLoader(), 
				new Class<?>[]{type}, new ViewInvocationHandler(view)));
	}

	static <T extends SchemaResource> T createSchemaProxy(Class<T> clazz, DataSource ds) {
		if(clazz.isInterface()) {
			var bind = scanBinding(clazz, false);
			scanResources(clazz.getMethods(), (t,c)-> c.isInterface()); // view or ComparisonExpression
			return clazz.cast(newProxyInstance(SchemaInvocationHandler.class.getClassLoader(), 
					new Class<?>[]{clazz}, new SchemaInvocationHandler(clazz, nonNull(bind) ? bind.value() : null, null, ds)));
		}
		throw new IllegalArgumentException("schema type must be an interface : " + clazz);
	}	
}
