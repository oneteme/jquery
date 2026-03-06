package org.usf.jquery.core;

import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Operators.function;
import static org.usf.jquery.core.Parameter.required;
import static org.usf.jquery.core.Provider.H2;

/**
 * 
 * @author u$f
 * 
 */
public final class H2Dialect extends Dialect {

	public H2Dialect() {
		super(H2);
	}

	@Override
	public OperatorDefinition bitAnd() {
		return new OperatorDefinition(BIGINT, function("BITAND"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitOr() {
		return new OperatorDefinition(BIGINT, function("BITOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitXor() {
		return new OperatorDefinition(BIGINT, function("BITXOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitNot() {
		return new OperatorDefinition(BIGINT, function("BITNOT"), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitShiftLeft() {
		return new OperatorDefinition(BIGINT, function("LSHIFT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public OperatorDefinition bitShiftRight() {
		return new OperatorDefinition(BIGINT, function("RSHIFT"), required(BIGINT), required(INTEGER));
	}
}
