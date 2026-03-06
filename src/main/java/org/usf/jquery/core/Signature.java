package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.function.BiFunction;
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
public final class Signature { 

	//there is no Singleton implementation, dummy sonar rule
	static final Signature NO_PARAM = new Signature(0, new Parameter[0]);
	
	private final int minArgs;
	private final Parameter[] parameters;

	public void match(Object... args) {
		var nArgs = isNull(args) ? 0 : args.length;
		traverse(checkArgsCount(nArgs), (p,i)-> {
			if(!p.accept(i, args)) {
				throw badArgumentTypeException(args[i], p.types(args));
			}
		});
	}

	public Object[] buildArgs(int nArgs, BiFunction<Integer, JavaType[], Object> fn) {
		var args = new Object[checkArgsCount(nArgs)];
		traverse(args.length, (p,i)-> args[i] = fn.apply(i, p.types(args)));
		return args;
	}

	void traverse(int nArgs, ObjIntConsumer<Parameter> cons) {
		var i=0;
		var min = min(nArgs, parameters.length);
		for(; i<min; i++) {
			cons.accept(parameters[i], i);
		}
		if(i<nArgs) {
			var last = parameters[parameters.length-1];
			for(; i<nArgs; i++) {
				cons.accept(last, i);
			}
		}
	}
	
	int checkArgsCount(int nArgs) {
		if(nArgs < minArgs || (nArgs > parameters.length && !isVarags())) {
			throw new SignatureMismatchException(format("expected %d%s arguments, but was %d", minArgs, isVarags() ? "+" : "", nArgs));
		}
		return nArgs;
	}

	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}

	public static Signature compile(Parameter... parameters) {
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
		return new Signature(nReqArgs, parameters);
	}

	@Override
	public String toString() {
		var s = "";
		if(parameters.length > 0) {
			s = Stream.of(parameters).limit(minArgs).map(Parameter::toString).collect(joining(", "));
			if(parameters.length > minArgs) {
				if(minArgs > 0) {
					s += ", ";
				}
				s += "[" + Stream.of(parameters).skip(minArgs).map(Parameter::toString).collect(joining(", "));
				if(parameters[parameters.length-1].isVarargs()) {
					s += "...";
				}
				s += "]";
			}
		}
		return "(" + s + ")";
	}
	
	public static SignatureMismatchException badArgumentTypeException(Object obj, JavaType[] types) {
		var exp = Parameter.toString(types);
		var arg = obj instanceof Typed t ? t.getType() : JDBCType.typeOf(obj).orElse(null);
		return new SignatureMismatchException(format("expected argument of type %s, but was %s [%s]", exp, obj, arg));
	}
}
