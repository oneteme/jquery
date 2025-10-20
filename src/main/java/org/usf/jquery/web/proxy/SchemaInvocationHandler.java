package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Proxy.newProxyInstance;
import static java.util.Arrays.stream;
import static java.util.Objects.hash;
import static org.usf.jquery.web.proxy.JQueryResource.getView;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupBindAnnotation;
import static org.usf.jquery.web.proxy.ResourceUtils.lookupResourceAnnotation;
import static org.usf.jquery.web.proxy.ViewInvocationHandler.validateViewResources;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaInvocationHandler implements InvocationHandler {
	
	private final String name;
	
	@Override
	@SuppressWarnings("unchecked")
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			return InvocationHandler.invokeDefault(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			var type = method.getReturnType();
			if(ViewResource.class.isAssignableFrom(type)) {
				return getView((Class<ViewResource>)type, method.getAnnotation(Bind.class), name); //cache ?
			}
			throw new IllegalStateException("illegal method " + method);
		}
		return switch (method.getName()) {
		case "equals"-> proxy == args[0];
		case "hashCode"-> hash(name);
		case "toString"-> name;
		default -> throw new IllegalStateException("unexpected method invocation " + method);
		};
	}

	static <T extends SchemaResource> T schemaProxy(Class<T> clazz) {
		var name = validateSchemaResources(clazz);
		return clazz.cast(newProxyInstance(SchemaInvocationHandler.class.getClassLoader(), 
				new Class<?>[]{clazz}, new SchemaInvocationHandler(name)));
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
