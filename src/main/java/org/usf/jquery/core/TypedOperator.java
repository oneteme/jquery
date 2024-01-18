package org.usf.jquery.core;

import static java.util.function.UnaryOperator.identity;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import java.util.function.UnaryOperator;

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
	private UnaryOperator<Object[]> argMapper = identity();
	
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
		args = parameterSet.args(args);
		return new OperationColumn(operator, argMapper.apply(args), typeFn.apply(args));
	}
	
	public Operator unwrap() {
		return operator;
	}

	TypedOperator argsMapper(UnaryOperator<Object[]> argMapper) {
		this.argMapper = argMapper;
		return this;
	}
}
