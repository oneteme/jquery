package org.usf.jquery.web.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

/**
 * 
 * @author u$f
 *
 */
public interface ArgumentsEvaluator {

	default Object[] evaluate(Method m, Entry[] args, QueryContext ctx) {
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
}