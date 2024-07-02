package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

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
	private final ArgTypeRef typeFn;
	private final ParameterSet parameterSet;
	
	public TypedOperator(JavaType type, Operator function, Parameter... args) {
		this(o-> type, function, args);
	}

	public TypedOperator(ArgTypeRef typeFn, Operator function, Parameter... parameter) {
		this.operator = function;
		this.typeFn = typeFn;
		this.parameterSet = ofParameters(parameter);
	}
	
	@Override
	public OperationColumn args(Object... args) {
		args = parameterSet.assertArguments(args);
		return new OperationColumn(operator, args, typeFn.apply(args));
	}
	
	public Operator unwrap() {
		return operator;
	}

	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
