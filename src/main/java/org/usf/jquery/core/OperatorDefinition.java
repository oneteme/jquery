package org.usf.jquery.core;

/**
 * 
 * @author u$f
 *
 */
public final class OperatorDefinition extends Definition<Column> {

	public OperatorDefinition(JDBCType type, Operator operator, Parameter... parameter) {
		super(operator.id(), type, operator, parameter);
	}
	
	public OperatorDefinition(String name, JDBCType type, Operator operator, Parameter... parameter) {
		super(name, type, operator, parameter);
	}

	public OperatorDefinition(TypeResolver typeFn, Operator operator, Parameter... parameter) {
		super(operator.id(), typeFn, operator, parameter);
	}
	
	public OperatorDefinition(String name, TypeResolver typeFn, Operator operator, Parameter... parameter) {
		super(name, typeFn, operator, parameter);
	}

	@Deprecated(forRemoval = true)
	public boolean isCountFunction() {
		return false;
	}
}
