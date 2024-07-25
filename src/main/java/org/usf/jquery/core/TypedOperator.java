package org.usf.jquery.core;

import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator implements Operator {
	
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

	@Override
	public String id() {
		return operator.id();
	}
	@Override
	public String sql(QueryParameterBuilder builder, Object[] args) {
		return operator.sql(builder, parameterSet.assertArguments(args));
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
