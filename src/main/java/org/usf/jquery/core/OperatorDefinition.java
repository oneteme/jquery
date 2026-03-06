package org.usf.jquery.core;

import static java.util.Objects.isNull;

/**
 * 
 * @author u$f
 *
 */
public final class OperatorDefinition extends Definition<Column> {

	public OperatorDefinition(JDBCType type, Operator operator, Parameter... parameter) {
		this(o-> type, operator, parameter);
	}

	public OperatorDefinition(TypeResolver typeFn, Operator operator, Parameter... parameter) {
		super(operator.id(), typeFn, (t,args)-> apply(operator, t, args), parameter);
	}

	static Column apply(Operator operator, JavaType type, Object[] args) {
		if(isNull(type) || type instanceof JDBCType) {
			return operator.operation((JDBCType)type, args);
		}
		throw new IllegalArgumentException("operator " + operator.id() + " cannot be applied to type " + type);
	}

	@Deprecated(forRemoval = true)
	public boolean isCountFunction() {
		return false;
	}
}
