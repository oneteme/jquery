package org.usf.jquery.web.proxy;

import static java.lang.reflect.Array.newInstance;
import static java.util.Objects.nonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * 
 * @author u$f
 *
 */
public interface ArgumentsEvaluator {

	default Object[] evaluate(Method m, Entry[] args, RequestContext ctx) {
		var nArgs = nonNull(args) ? args.length : 0;
		var params = m.getParameters();
		if(params.length == 1 && params[0].getType().isArray()) {
			var type = params[0].getType().getComponentType();
			var arr = newInstance(type, nArgs);
			for(int i=0; i<nArgs; i++) {
				Array.set(arr, i, ctx.resolve(args[i], type));
			}
			return new Object[] {arr}; //allow empty array ?
		}
		if(params.length == nArgs) {
			var arr = new Object[nArgs];
			for(int i=0; i<nArgs; i++) {
				arr[i] = ctx.resolve(args[i], params[i].getType());
			}
			return arr;
		}
		throw new IllegalArgumentException("expected " + params.length + " arguments but got " + nArgs);
	}
}