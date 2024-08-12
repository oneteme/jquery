package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator {
	
	private final Operator operator;
	private final ArgTypeRef typeFn;
	private final ParameterSet parameterSet;
	
	public TypedOperator(JDBCType type, Operator function, Parameter... parameter) {
		this(o-> type, function, parameter);
	}

	public TypedOperator(ArgTypeRef typeFn, Operator function, Parameter... parameter) {
		this.operator = function;
		this.typeFn = typeFn;
		this.parameterSet = ofParameters(parameter);
	}

	public OperationColumn args(Object... args) {
		args = parameterSet.assertArguments(args);
		return operator.args(typeFn.apply(args), args);
	}
	
	public Operator unwrap() {
		return operator;
	}

	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
