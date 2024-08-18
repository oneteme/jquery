package org.usf.jquery.core;

import static org.usf.jquery.core.BadArgumentException.badArgumentsException;
import static org.usf.jquery.core.ParameterSet.ofParameters;

import java.util.Arrays;

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
			throw badArgumentsException(this.toString(), Arrays.toString(args), e);
		}
	}
	
	public Operator unwrap() {
		return operator;
	}

	@Override
	public String toString() {
		return operator.id() + parameterSet.toString();
	}
}
