package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.BadArgumentException.badArgumentTypeException;

import java.util.function.ObjIntConsumer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterSet {

	private static final Parameter[] NO_PARAM = new Parameter[0];
	
	private final int nReqArgs;
	private final Parameter[] parameters;
	
	public Object[] args(Object... args) {
		var arr = isNull(args) ? NO_PARAM : args;
		forEach(arr.length, (p,i)-> {
			if(!p.accept(i, arr)) {
				throw badArgumentTypeException(p.types(args), arr[i]);
			}
		});
		return arr;
	}

	public void forEach(int nArgs, ObjIntConsumer<Parameter> cons) {
		if(nArgs < nReqArgs || (nArgs > parameters.length && !isVarags())) {
			throw badArgumentCountException(nReqArgs, nArgs);
		}
		var i=0;
		for(; i<min(nArgs, parameters.length); i++) {
			cons.accept(parameters[i], i);
		}
		if(i<nArgs) {
			var last = parameters[parameters.length-1];
			for(; i<nArgs; i++) {
				cons.accept(last, i);
			}
		}
	}

	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}
	
	public static ParameterSet ofParameters(Parameter... parameters) {
		if(isNull(parameters)) {
			return new ParameterSet(0, NO_PARAM);
		}
		var i=0;
		for(; i<parameters.length && parameters[i].isRequired(); i++) {
			if(parameters[i].isVarargs() && i<parameters.length-1) {
				throw new IllegalArgumentException("varargs should be the last parameter");
			}
		}
		var nReqArgs = i;
		for(; i<parameters.length && !parameters[i].isRequired(); i++) {
			if(parameters[i].isVarargs() && i<parameters.length-1) {
				throw new IllegalArgumentException("varargs should be the last parameter");
			}
		}
		if(i<parameters.length) {
			throw new IllegalArgumentException("required parameter cannot follow optional parameter");
		}
		return new ParameterSet(nReqArgs, parameters);
	}
}
