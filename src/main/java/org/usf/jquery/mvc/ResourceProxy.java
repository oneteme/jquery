package org.usf.jquery.mvc;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.usf.jquery.mvc.Bind.BindType.REF;
import static org.usf.jquery.mvc.MethodUtils.getMethod;
import static org.usf.jquery.mvc.ResourceInvoker.ofMethod;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author u$f
 *
 */
@Slf4j
@Getter
public abstract class ResourceProxy implements InvocationHandler {

	private static final Method LOOKUP_METHOD = getMethod("lookup", Resource.class, String.class, Class.class);

	final Map<String, Method> exposedMethods;
	final Map<Method, Object> resourcesCache;
	final Set<Method> excludes;
	
	ResourceProxy(Map<String, Method> exposedMethods, Map<Method, Object> resourcesCache) {
		this.exposedMethods = nonNull(exposedMethods) ? unmodifiableMap(exposedMethods) : emptyMap();
		this.resourcesCache = nonNull(resourcesCache) ? unmodifiableMap(resourcesCache) : emptyMap();
		this.excludes = this.exposedMethods.values().stream().filter(m->{
			var exp = m.getAnnotation(Expose.class);
			return nonNull(exp) && !exp.value();
		}).collect(toUnmodifiableSet());
	}
	
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(resourcesCache.containsKey(method)) { //contains key but value can be null
			return resourcesCache.get(method);
		}
		if(method.isDefault()) {
			return invokeDefaultMethod(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			if(LOOKUP_METHOD.equals(method)) {
				return invokeLookupMethod(proxy, (String)args[0], (Class<?>)args[1]);
			}
			return invokeAbstractMethod(proxy, method, args);
		}
		return switch (method.getName()) {
		case "equals"-> invokeEquals(proxy, args);
		case "hashCode"-> invokeHashCode(proxy, args);
		case "toString"-> invokeToString(proxy, args);
		default -> throw new IllegalStateException("unexpected method invocation " + method);
		};
	}
	
	Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
		return InvocationHandler.invokeDefault(proxy, method, args);
	}
	
	<T> ResourceInvoker<T> invokeLookupMethod(Object proxy, String name, Class<T> type) {
		var mth = exposedMethods.get(name);
		if(nonNull(mth) && type.isAssignableFrom(mth.getReturnType())) {
			return ofMethod(!excludes.contains(mth), mth, proxy);
		}
		return null;
	}
	
	Object invokeAbstractMethod(Object proxy, Method method, Object[] args) {
		throw new IllegalStateException("unexpected method invocation : " + method);
	}
	
	boolean invokeEquals(Object proxy, Object[] args) {
		return proxy == args[0];
	}
	
	abstract int invokeHashCode(Object proxy, Object[] args);
	
	abstract String invokeToString(Object proxy, Object[] args);
	
	static Map<String, Method> discoverExposedMethods(Class<?> type, BiPredicate<Method, Bind> pre) {
		return stream(type.getDeclaredMethods()).filter(m-> {
			var mod = m.getModifiers();
			if(!isStatic(mod) && isPublic(mod)) {
				var bnd = validateBind(m); //no arguments
				if(nonNull(bnd) && !pre.test(m, bnd)) {
					throw new ResourceMappingException("invalid @Bind.type=["+bnd+"] for return type " + m + " on " + m);
				}
				return true;
			}
			return false;
		}).collect(toMap(ResourceProxy::validateExpose, identity(), (m1, m2) -> {
			throw new ResourceMappingException("duplicate resource identifier: " + m1.getName() + " vs " + m2.getName());
		}));
	}
	
	static Bind validateBind(Method m){
		Bind bnd = null;
		if(isAbstract(m.getModifiers())) {
			bnd = scanBind(m);
			if(m.getParameterCount() > 0) { 
				throw new ResourceMappingException("binded method cannot have parameters : " + m);
			}
		}
		else if(nonNull(m.getAnnotation(Bind.class))) { //bind default method 
			throw new ResourceMappingException("default method cannot be binded : " + m);
		}
		return bnd;
	}
	
	static String validateExpose(Method m){
		var exp = m.getAnnotation(Expose.class); //optional annotation for exposed method
		if(nonNull(exp)) {
			if(!exp.alias().isEmpty()) {
				verifyIdentifier(exp.alias(), () -> "invalid @Expose.alias=["+exp.alias()+"] on " + m);
			}
			if(!exp.identity().isEmpty()) {
				verifyIdentifier(exp.identity(), () -> "invalid @Expose.id=["+exp.identity()+"] on " + m);
				return exp.identity();
			}
		} //TD check reserved words
		return m.getName();
	}
	
	static Bind scanBind(AnnotatedElement elem){
		var bnd = elem.getAnnotation(Bind.class);
		if(nonNull(bnd)) {
			if(bnd.type() == REF) {
				verifyIdentifier(bnd.value(), () -> "invalid @Bind.value=["+bnd.value()+"] on " + elem);
			}	
			return bnd;
		}
		throw new ResourceMappingException("missing decorator @Bind on " + elem);
	}
		
	static void verifyIdentifier(String id, Supplier<String> message) {
		if(isNull(id) || !id.matches("[a-zA-Z_]\\w*")) {
			throw new ResourceMappingException(message.get());
		}
	}
}
