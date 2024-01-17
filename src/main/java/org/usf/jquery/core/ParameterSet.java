package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;

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
	
	private final Parameter[] parameters;
	
	public Object[] args(Object... args) {
		var arr = isNull(args) ? new Object[0] : args;
		forEach(arr.length, (p,i)-> {
			if(!p.accept(i, arr)) {
				throw badArgumentTypeException();
			}
		});
		return arr;
	}

	public void forEach(int nArgs, ObjIntConsumer<Parameter> cons) {
		var rq = requiredParameterCount();
		if(nArgs < rq || (nArgs > parameters.length && !isVarags())) {
			throw badArgumentCountException();
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

	public int requiredParameterCount() {
		var i=0;
		while(i<parameters.length && parameters[i].isRequired()) i++;
		return i;
	}

	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}
	
	public static ParameterSet ofParameters(Parameter... parameters) {
		if(isNull(parameters)) {
			return new ParameterSet(NO_PARAM);
		}
		var i=0;
		for(; i<parameters.length && parameters[i].isRequired(); i++) {
			if(parameters[i].isVarargs() && i<parameters.length-1) {
				throw new IllegalArgumentException("varargs should be the last parameter");
			}
		}
		for(; i<parameters.length && !parameters[i].isRequired(); i++) {
			if(parameters[i].isVarargs() && i<parameters.length-1) {
				throw new IllegalArgumentException("varargs should be the last parameter");
			}
		}
		if(i<parameters.length) {
			throw new IllegalArgumentException("required parameter cannot follow optional parameter");
		}
		return new ParameterSet(parameters);
	}
	
	private static IllegalArgumentException badArgumentTypeException() {
		return new IllegalArgumentException("bad argument type");
	}
	
	private static IllegalArgumentException badArgumentCountException() {
		return new IllegalArgumentException("bad argument count");
	}
	
}
