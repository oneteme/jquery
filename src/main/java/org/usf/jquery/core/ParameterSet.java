package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.BadArgumentException.badArgumentCountException;
import static org.usf.jquery.core.BadArgumentException.badArgumentTypeException;

import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

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
public final class ParameterSet { //there is no Singleton implementation, dummy sonar rule

	static final ParameterSet NO_PARAM = new ParameterSet(0, new Parameter[0]);
	
	private final int nReqArgs;
	private final Parameter[] parameters;

	public Object[] assertArguments(Object... args) {
		return assertArgumentsFrom(0, args);
	}
	
	public Object[] assertArgumentsFrom(int idx, Object... args) {
		var arr = isNull(args) ? new Object[0] : args;
		forEach(arr.length, (p,i)-> {
			if(i>=idx && !p.accept(i, arr)) {
				throw badArgumentTypeException(p.types(arr), arr[i]);
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
			return NO_PARAM;
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

	@Override
	public String toString() {
		var s = "";
		if(parameters.length > 0) {
			s = Stream.of(parameters).limit(nReqArgs).map(Parameter::toString).collect(joining(", "));
			if(parameters.length > nReqArgs) {
				if(nReqArgs > 0) {
					s += ", ";
				}
				s += "[" + Stream.of(parameters).skip(nReqArgs).map(Parameter::toString).collect(joining(", "));
				if(parameters[parameters.length-1].isVarargs()) {
					s += "...";
				}
				s += "]";
			}
		}
		return "(" + s + ")";
	}
}
