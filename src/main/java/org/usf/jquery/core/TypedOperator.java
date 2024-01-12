package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

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
	
	@Delegate
	private final Operator operator;
	private final Function<Object[], JavaType> typeFn;
	private final ParameterSet parameterSet;
	
	public TypedOperator(JavaType type, Operator function, Parameter... args) {
		this(o-> type, function, args);
	}

	public TypedOperator(Function<Object[], JavaType> typeFn, Operator function, Parameter... parameter) {
		this.operator = function;
		this.typeFn = typeFn;
		this.parameterSet = ofParameters(parameter);
	}
	
	public Operator unwrap() {
		return operator;
	}
	
	@Override
	public OperationColumn args(Object... args) {
		args = parameterSet.match(args);
		return new OperationColumn(operator, afterCheck(args), typeFn.apply(args));
	}
	
	Object[] afterCheck(Object... args) {
		return args;
	}
}
