package org.usf.jquery.core;


import static org.usf.jquery.core.JDBCType.BIGINT;
import static org.usf.jquery.core.JDBCType.INTEGER;
import static org.usf.jquery.core.Parameter.required;

/**
 * 
 * @author u$f
 * 
 */
public class H2Operators extends Operators {

	@Override
	public TypedOperator bitAnd() {
		return new TypedOperator(BIGINT, function("BITAND"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitOr() {
		return new TypedOperator(BIGINT, operator("BITOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitXor() {
		return new TypedOperator(BIGINT, operator("BITXOR"), required(BIGINT), required(BIGINT));
	}

	@Override
	public TypedOperator bitNot() {
		return new TypedOperator(BIGINT, operator("BITNOT"), required(BIGINT));
	}

	@Override
	public TypedOperator bitShiftLeft() {
		return new TypedOperator(BIGINT, operator("LSHIFT"), required(BIGINT), required(INTEGER));
	}

	@Override
	public TypedOperator bitShiftRight() {
		return new TypedOperator(BIGINT, operator("RSHIFT"), required(BIGINT), required(INTEGER));
	}
	
}
