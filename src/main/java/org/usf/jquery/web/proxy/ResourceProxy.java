package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.usf.jquery.web.EntryParseException;

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
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ResourceProxy implements InvocationHandler, ArgumentsEvaluator {

	private static final Method INVOKE_METHOD;
	private static final Method EXPOSES_METHOD;

	private final Map<String, Method> exposedMethods;
	
	static {
		try {
			INVOKE_METHOD = Resource.class.getMethod("invokeResource", String.class, Class.class, Entry[].class, RequestContext.class);
			EXPOSES_METHOD = Resource.class.getMethod("exposes", String.class, Class.class);
		} catch (Exception e) {
			throw new NoSuchMethodError("failed to initialize ResourceProxy: " + e.getMessage());
		}
	}
	
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			return invokeDefaultMethod(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			if(method.equals(EXPOSES_METHOD)) {
				return invokeExposesMethod(assertArguments(EXPOSES_METHOD, args));
			}
			if(method.equals(INVOKE_METHOD)) {
				return invokeResourceMethod(proxy, assertArguments(INVOKE_METHOD, args));
			}
			else {
				return invokeAbstractMethod(proxy, method, args);
			}
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
	
	boolean invokeExposesMethod(Object[] args) {
		var m = exposedMethods.get(args[0]);
		return nonNull(m) && ((Class<?>)args[1]).isAssignableFrom(m.getReturnType());
	}
	
	Object invokeResourceMethod(Object proxy, Object[] args) {
		var m = exposedMethods.get(args[0]);
		if(nonNull(m) && ((Class<?>)args[1]).isAssignableFrom(m.getReturnType())) {
			Object[] arr = null;
			var entries = (Entry[]) args[2];
			if(m.getParameterCount() > 0) {
				try {
					 arr = (proxy instanceof ArgumentsEvaluator eval ? eval : this).evaluate(m, entries, (RequestContext) args[3]);
				}
				catch (EntryParseException e) {
					throw e;
				}
				catch (Exception e) {
					throw new EntryParseException("failed to parse arguments for method " + m.getName(), e);
				}
			}
			else if(!isEmpty(entries)){
				throw new ResourceInvocationException("method " + m.getName() + " does not expect arguments");
			}
			try {
				return invoke(proxy, m, arr);
			}
			catch (Throwable e) {
				throw new ResourceInvocationException(e);
			}
		}
		return null;		
	}
	
	boolean invokeEquals(Object proxy, Object[] args) {
		return proxy == args[0];
	}
	
	abstract Object invokeAbstractMethod(Object proxy, Method method, Object[] args);
	
	abstract int invokeHashCode(Object proxy, Object[] args);
	
	abstract String invokeToString(Object proxy, Object[] args);
	
	static Object[] assertArguments(Method m, Object... args) {
		var nArgs = isNull(args) ? 0 : args.length;
		if(m.getParameterCount() == nArgs) {
			if(nArgs > 0) {
				var params = m.getParameters();
				for(int i=0; i<nArgs; i++) {
					if(nonNull(args[i]) && !params[i].getType().isInstance(args[i])) {
						throw new IllegalArgumentException("expected argument " + i + " to be of type " + m.getParameters()[i].getType() + " but got " + args[i].getClass());
					}
				}
			}
			return args;
		}
		throw new IllegalArgumentException("expected " + m.getParameterCount() + " arguments but got " + nArgs);
	}
}
