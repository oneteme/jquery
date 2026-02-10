package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.DBView.view;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupBindAnnotation;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupResourceAnnotation;
import static org.usf.jquery.web.proxy.ViewInvocationHandler.validateViewResources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.usf.jquery.core.DBView;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaInvocationHandler implements InvocationHandler {
	
	private final String name;
	private final String schema;
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			return InvocationHandler.invokeDefault(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			var bind = method.getAnnotation(Bind.class); //required
			if(nonNull(bind) && !bind.value().isEmpty()) {
				return buildResource(method, bind, args);
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

	DBView buildResource(Method method, Bind bind, Object[] args) {
		var type = method.getReturnType();
		if(DBView.class.isAssignableFrom(type)) {
			var view = switch(bind.type()) {
			case REF-> view(bind.value(), schema);
//			case REQ-> evalView(parseEntry(bind.value()), null)
			default -> throw new UnsupportedOperationException("not implemented");
			};
			return (DBView)(newProxyInstance(ViewInvocationHandler.class.getClassLoader(), 
					new Class<?>[]{type}, new ViewInvocationHandler(view)));
		}
		throw new IllegalStateException("unsupported method type " + method);
	}

	static <T extends Resource> T createSchemaProxy(Class<T> clazz) {
		var name = validateSchemaResources(clazz);
		return clazz.cast(newProxyInstance(SchemaInvocationHandler.class.getClassLoader(), 
				new Class<?>[]{clazz}, new SchemaInvocationHandler(name, null))); //TODO parse schema from annotation
	}
	
	static String validateSchemaResources(Class<?> clazz) {
		if(clazz.isInterface()) {
			var name = lookupBindAnnotation(clazz).map(Bind::value).orElse(null); //optional
			stream(clazz.getMethods()).forEach(mth->{
				lookupBindAnnotation(mth, c->{
					if(c == ViewResource.class) {
						throw new IllegalArgumentException(mth.getName() + " must return a type that extends 'ViewResource.class'");
					}
					if(ViewResource.class.isAssignableFrom(c)) {
						return t-> true; //REF|REQ|SQL
					}
					throw new IllegalArgumentException("illegal return type " + mth);
				});
				lookupResourceAnnotation(mth);
				validateViewResources(mth.getReturnType());
			});
			return name;
		}
		throw new IllegalArgumentException(clazz + " is not a interface");
	}
}
