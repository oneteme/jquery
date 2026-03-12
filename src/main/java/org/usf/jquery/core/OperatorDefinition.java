package org.usf.jquery.core;

import static java.lang.String.format;
import static java.util.Objects.isNull;

/**
 * 
 * @author u$f
 *
 */
public final class OperatorDefinition extends Definition<Column> {
	
	private final Operator operator;
	private final OperatorKind kind;
	
	public OperatorDefinition(String name, TypeResolver typeFn, OperatorKind kind, Operator operator, Parameter... parameter) {
		super(name, typeFn, parameter);
		this.operator = operator;
		this.kind = kind;
	}

	@Override
	protected Column internalInvoke(JavaType type, Object... args) {
		if(isNull(type) || type instanceof JDBCType) {
			return new OperationColumn(getName(), kind, operator, args, (JDBCType)type);
		}
		throw new IllegalStateException(format("operator '%s' cannot be applied to type %s", this, type));
	}
}
