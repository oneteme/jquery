package org.usf.jquery.core;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * 
 * @author u$f
 *
 */
public final class OperatorDefinition extends Definition<Column> {
	
	private final Invocable operator;
	private final OperatorKind kind;
	
	public OperatorDefinition(String name, TypeResolver typeFn, OperatorKind kind, Invocable operator, Parameter... parameter) {
		super(name, typeFn, parameter);
		this.operator = operator;
		this.kind = kind;
	}

	@Override
	protected Column internalInvoke(JavaType type, Object... args) {
		if(isNull(type) || type instanceof JDBCType) {
			var arr = nonNull(args) ? asList(args) : emptyList();
			return new OperationColumn(getName(), kind, operator, arr, (JDBCType)type);
		}
		throw new IllegalStateException(format("operator '%s' cannot be applied to type %s", this, type));
	}
}
