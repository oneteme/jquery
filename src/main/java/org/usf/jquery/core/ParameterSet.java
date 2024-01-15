package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;

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
	
	public Object[] match(Object... args) {
		if(isNull(args)) {
			args = new Object[0];
		}
		var na = args.length;
		var rq = requireParameterCount();
		if(na >= rq && (na <= parameters.length || isVarags())) {
			var i=0;
			for(; i<min(na, parameters.length); i++) {
				if(!parameters[i].accept(args[i])) {
					throw argumentTypeMismatch();
				}
			}
			if(i<args.length) {
				var last = parameters[parameters.length-1];
				if(!last.accept(args[i])) {
					throw argumentTypeMismatch();
				}
			}
			return args;
		}
		throw argumentTypeMismatch();
	}

	public int parameterCount() {
		return parameters.length;
	}

	public int requireParameterCount() {
		var i=0;
		while(i<parameters.length && parameters[i].isRequired()) i++;
		return i;
	}

	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}
	
	public static ParameterSet ofParameters(Parameter... parameters) {
		if(isNull(parameters)) {
			parameters = NO_PARAM;
		}
		else {
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
		}
		return new ParameterSet(parameters);
	}
	
	private static IllegalArgumentException argumentTypeMismatch() {
		return new IllegalArgumentException("argument type mismatch");
	}
	
}
