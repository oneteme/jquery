package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * 
 * @author u$f
 *
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterSet {

	private static final Parameter[] NO_PARAM = new Parameter[0];
	
	private final Parameter[] parameters;
	
	public Object[] match(Object... args) {
		if(isNull(args)) {
			args = new Object[0];
		}
		var na = args.length;
		var rq = requireArgCount();
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

	public int requireArgCount() {
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
			while(i<parameters.length && parameters[i].isRequired()) {
				if(parameters[i].isVarargs() && i<parameters.length-1) {
					throw new IllegalArgumentException("varargs should be the last parameter");
				}
				i++;
			}
			for(; i<parameters.length; i++) {
				if(parameters[i].isRequired()) {
					throw new IllegalArgumentException("required parameter cannot follow optional parameter");
				}
				if(parameters[i].isVarargs() && i<parameters.length-1) {
					throw new IllegalArgumentException("varargs should be the last parameter");
				}
			}
		}
		return new ParameterSet(parameters);
	}
	
	private static IllegalArgumentException argumentTypeMismatch() {
		return new IllegalArgumentException("argument type mismatch");
	}
	
}
