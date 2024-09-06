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

	public OperationColumn operation(Object... args) {
		try {
			args = parameterSet.assertArguments(args);
			return operator.args(typeFn.apply(args), args);
		} catch (BadArgumentException e) {
			throw badArgumentsException("operator", operator.id(), args, e);
		}
	}

	public boolean isCountFunction() {
		return "COUNT".equals(operator.id());
	}
	
	public boolean isWindowFunction() {
		return operator instanceof WindowFunction;
	}
	
	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
