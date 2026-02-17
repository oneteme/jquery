package org.usf.jquery.web.proxy;

import static java.lang.reflect.Proxy.getInvocationHandler;
import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

public interface Resource {
	
	public static final EntryChain[] NO_PARAM = new EntryChain[0];
	
	//do not change this method signature, it is used by ResourceInvokerHandler to invoke resource method by id
	Method lookupMethod(String id, Class<?> type);

	<T> T invokeExposed(String id, Class<T> type, EntryChain[] args, QueryContext ctx);
	
	static Object invokeResource(Method method, Object proxy, EntryChain[] arguments, QueryContext ctx) {
		Object[] args = null;
		if(method.getParameterCount() > 0) {
			var ann = method.getAnnotation(Parameterized.class);
			ArgsParser prs = nonNull(ann) ? newInstance(ann.parser()) : Resource::parseArgs;
			try {
				 args = prs.parse(method, arguments, ctx);
			}
			catch (Exception e) {
				throw new EntryParseException("failed to parse arguments for method " + method.getName(), e);
			}
		}
		else if(!isEmpty(arguments)){
			throw new ResourceInvocationException("method " + method.getName() + " does not expect arguments");
		}
		try {
			var handler = getInvocationHandler(proxy);
			return nonNull(handler) ? handler.invoke(proxy, method, args) : method.invoke(proxy, args); //TODO check this
		}
		catch (Throwable e) {
			throw new ResourceInvocationException(e);
		}
	}
	
	static <T> T newInstance(Class<T> type){
		try {
			return type.getConstructor().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	static Object[] parseArgs(Method m, EntryChain[] args, QueryContext ctx) {
		var params = m.getParameters();
		if(params.length == 1 && params[0].getType().isArray()) {
			var type = params[0].getType().getComponentType();
			var arr = Array.newInstance(type, args.length);
			for(int i=0; i<args.length; i++) {
				Array.set(arr, i, ctx.eval(args[i], type));
			}
		}
		if(params.length == args.length) {
			var arr = new Object[params.length];
			for(int i=0; i<params.length; i++) {
				arr[i] = ctx.eval(args[i], params[i].getType());
			}
			return arr;
		}
		throw new IllegalArgumentException("expected " + params.length + " arguments but got " + args.length);
	}
}
