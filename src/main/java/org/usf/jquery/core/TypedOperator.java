package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import lombok.Getter;

/**
 * 
 * @author u$f
 *
 */
@Getter
public class TypedOperator implements Operator {
	
	private final Operator operator; // do not delegate
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
	public String sql(QueryVariables builder, Object[] args) {
		try {
			return operator.sql(builder, parameterSet.assertArguments(args));
		}
		catch (BadArgumentException e) {
			throw badArgumentsException("operator", operator.id(), args, e);
		}
	}
	
	@Override
	public boolean is(Class<? extends Operator> type) {
		return operator.is(type);
	}

	public OperationColumn operation(Object... args) {
		return Operator.super.operation(typeFn.apply(args), args);
	}

	public boolean isWindowFunction() {
		return operator.is(WindowFunction.class);
	}
	
	public boolean isCountFunction() {
		return operator.is("COUNT");
	}
	
	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
