package org.usf.jquery.web.proxy;

import static java.util.Objects.nonNull;
import static org.usf.jquery.core.Utils.isEmpty;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import org.usf.jquery.web.EntryParseException;
import org.usf.jquery.web.proxy.Parameterized.ArgsParser;

public interface Resource {
	
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
			return method.invoke(proxy, args);
		}
		catch (Exception e) {
			throw new ResourceInvocationException(e);
		}
	}
	
	static Method findMethod(Object proxy, String name) {
		var methods = proxy.getClass().getMethods();
		Method res = null;
		for(var m : methods) {
			var ann = m.getAnnotation(Entry.class); //entry annotation has higher priority than method name
			if(nonNull(ann) && ann.value().equals(name)) {
				return m;
			}
			if(m.getName().equals(name)) {
				res = m; //keep looking for entry annotation, method name is fallback
			}
		}
		return res;
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
