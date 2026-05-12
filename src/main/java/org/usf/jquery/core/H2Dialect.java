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
		return function(BIGINT, "BITAND", required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitOr() {
		return function(BIGINT, "BITOR", required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitXor() {
		return function(BIGINT, "BITXOR", required(BIGINT), required(BIGINT));
	}

	@Override
	public OperatorDefinition bitNot() {
		return function(BIGINT, "BITNOT", required(BIGINT));
	}

	@Override
	public OperatorDefinition bitShiftLeft() {
		return function(BIGINT, "LSHIFT", required(BIGINT), required(INTEGER));
	}

	@Override
	public OperatorDefinition bitShiftRight() {
		return function(BIGINT, "RSHIFT", required(BIGINT), required(INTEGER));
	}
	
	@Override
	public boolean supportGroupByIndex() {
		return false;
	}
	
	@Override
	public boolean supportGroupByAlias() {
		return true;
	}
	
	@Override
	public boolean supportHavingByAlias() {
		return true;
	}
}
