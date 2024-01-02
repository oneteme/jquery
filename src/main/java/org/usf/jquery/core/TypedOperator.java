package org.usf.jquery.core;

import static java.lang.Math.min;
import static java.util.Objects.isNull;
import static org.usf.jquery.core.Parameter.checkParams;

import java.util.function.BiConsumer;
import java.util.function.Function;

import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator implements Operator {
	
	private static final Parameter[] NO_PARAM = new Parameter[0];

	@Delegate
	private final Operator operator;
	private final Function<Object[], JavaType> typeFn;
	private final Parameter[] parameters;
	
	public TypedOperator(JavaType type, Operator function, Parameter... args) {
		this(o-> type, function, args == null ? NO_PARAM : args);
	}

	public TypedOperator(Function<Object[], JavaType> typeFn, Operator function, Parameter... parameter) {
		this.typeFn = typeFn;
		this.operator = function;
		this.parameters = checkParams(parameter);
	}
	
	public Operator unwrap() {
		return operator;
	}
	
	@Override
	public OperationColumn args(Object... args) {
		return args(args, (p, o)->{
			if(!p.accept(o)) {
				throw new IllegalArgumentException("mismatch arg type");
			}
		});
	}

	OperationColumn args(Object[] args, BiConsumer<Parameter, Object> fn) {
		if(isNull(args)) {
			args = new Object[0];
		}
		var na = args.length;
		var rq = requireArgCount();
		if(na >= rq && (na <= parameters.length || isVarags())) {
			var i=0;
			for(; i<min(na, parameters.length); i++) {
				fn.accept(parameters[i], args[i]);
			}
			if(i<args.length) {
				var last = parameters[parameters.length-1];
				fn.accept(last, args[i]);
			}
			return new OperationColumn(operator, args, typeFn.apply(args));
		}
		throw new IllegalArgumentException();
	}

	public int requireArgCount() {
		var i=0;
		while(i<parameters.length && parameters[i].isRequired()) i++;
		return i;
	}
	
	public boolean isVarags() {
		return parameters.length > 0 && parameters[parameters.length-1].isVarargs(); 
	}
}
