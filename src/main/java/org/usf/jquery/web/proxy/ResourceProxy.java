package org.usf.jquery.web.proxy;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

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
public abstract class ResourceProxy implements InvocationHandler {

	private static final Method INVOKE_METHOD;
	private static final Method EXPOSES_METHOD;

	private final Map<String, Method> exposedMethods;
	
	static {
		try {
			INVOKE_METHOD = Resource.class.getMethod("invokeResource", String.class, Class.class, Entry[].class, QueryContext.class);
			EXPOSES_METHOD = Resource.class.getMethod("exposes", String.class, Class.class);
		} catch (Exception e) {
			throw new NoSuchMethodError("failed to initialize ResourceInvokerHandler: " + e.getMessage());
		}
	}
	
	@Override
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if(method.isDefault()) {
			return invokeDefaultMethod(proxy, method, args);
		}
		if(isAbstract(method.getModifiers())) {
			if(method == EXPOSES_METHOD) {
				return invokeExposesMethod(assertArguments(EXPOSES_METHOD, args));
			}
			if(method == INVOKE_METHOD) {
				return invokeResourceMethod(proxy, assertArguments(INVOKE_METHOD, args));
			}
			else {
				var bind = method.getAnnotation(Bind.class);
				if(nonNull(bind)) {
					return invokeAbstractMethod(proxy, bind, method, args);
				}
				throw new IllegalStateException("unexpected abstract method invocation " + method);
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
		return nonNull(m) && m.getReturnType() == args[1];
	}
	
	Object invokeResourceMethod(Object proxy, Object[] args) {
		var m = exposedMethods.get(args[0]);
		if(nonNull(m) && m.getReturnType() == args[1]) {
			Object[] arr = null;
			var entries = (Entry[]) args[2];
			if(m.getParameterCount() > 0) {
				var ann = m.getAnnotation(Parameterized.class);
				ArgsParser prs = nonNull(ann) ? newInstance(ann.parser()) : ResourceProxy::parseArgs;
				try {
					 arr = prs.parse(m, entries, (QueryContext) args[3]);
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
	
	abstract Object invokeAbstractMethod(Object proxy, Bind bind, Method method, Object[] args);
	
	abstract int invokeHashCode(Object proxy, Object[] args);
	
	abstract String invokeToString(Object proxy, Object[] args);
	
	static <T> T newInstance(Class<T> type){
		try {
			return type.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("failed to create instance of " + type.getName(), e);
		}
	}
	
	static Object[] parseArgs(Method m, Entry[] args, QueryContext ctx) {
		var params = m.getParameters();
		if(params.length == 1 && params[0].getType().isArray()) {
			var type = params[0].getType().getComponentType();
			var arr = Array.newInstance(type, args.length);
			for(int i=0; i<args.length; i++) {
				Array.set(arr, i, ctx.resolve(args[i], type));
			}
			return new Object[] {arr};
		}
		if(params.length == args.length) {
			var arr = new Object[params.length];
			for(int i=0; i<params.length; i++) {
				arr[i] = ctx.resolve(args[i], params[i].getType());
			}
			return arr;
		}
		throw new IllegalArgumentException("expected " + params.length + " arguments but got " + args.length);
	}

	static Object[] assertArguments(Method m, Object... args) {
		var nArgs = isNull(args) ? 0 : args.length;
		if(m.getParameterCount() == nArgs) {
			if(nArgs > 0) {
				var params = m.getParameters();
				for(int i=0; i<nArgs; i++) {
					if(!params[i].getType().isInstance(args[i])) {
						throw new IllegalArgumentException("expected argument " + i + " to be of type " + m.getParameters()[i].getType() + " but got " + args[i].getClass());
					}
				}
			}
			return args;
		}
		throw new IllegalArgumentException("expected " + m.getParameterCount() + " arguments but got " + nArgs);
	}
}
