package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;
import static org.usf.jquery.core.Utils.isEmpty;

import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

import org.usf.jquery.core.JavaType.Typed;

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
		var arr = isNull(args) ? new Object[0] : args;
		eachParameter(arr.length, (p,i)-> {
			if(!p.accept(i, arr)) {
				throw badArgumentTypeException(arr[i], p.types(arr));
			}
		});
		return arr;
	}

	public void eachParameter(int nArgs, ObjIntConsumer<Parameter> cons) {
		if(nArgs < nReqArgs || (nArgs > parameters.length && !isVarags())) {
			throw badArgumentCountException(nArgs, nReqArgs);
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
	
	private static BadArgumentException badArgumentCountException(int count, int expect) {
		return new BadArgumentException(format("expected %d arguments, but was %d", count, expect));
	}

	private static BadArgumentException badArgumentTypeException(Object obj, JavaType[] types) {
		var par = isEmpty(types) ? "any" : stream(types).map(Object::toString).collect(joining("|"));
		var arg = obj instanceof Typed t ? t.getType() : JDBCType.typeOf(obj).orElse(null);
		return new BadArgumentException(format("expected argument of type %s, but was %s [%s]", par, obj, arg));
	}
}
